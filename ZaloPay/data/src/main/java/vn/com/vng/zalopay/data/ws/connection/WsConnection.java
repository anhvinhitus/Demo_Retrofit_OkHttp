package vn.com.vng.zalopay.data.ws.connection;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.WsConnectionEvent;
import vn.com.vng.zalopay.data.rxbus.RxBus;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.ServerPongData;
import vn.com.vng.zalopay.data.ws.parser.Parser;
import vn.com.vng.zalopay.data.ws.protobuf.ServerMessageType;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 6/14/16.
 * Network handlers for Socket connection
 */
public class WsConnection extends Connection {
    private static final int TIMER_HEARTBEAT = 60;
    private static final int SERVER_TIMEOUT = 2 * 60;
    private static final int TIMER_CONNECTION_CHECK = 10;
    private String gcmToken;

    private final Context context;

    private int numRetry;

    private final Parser parser;
    private final UserConfig userConfig;

    private SocketClient mSocketClient;
    private RxBus mServerPongBus;

    private final Handler mConnectionHandler;
    private Long mCheckCountDown = 100L;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    /**
     * Next connection state.
     * If request to disconnect: set mNextConnectionState = DISCONNECT
     * If network connection is lost due to error: set mNextConnectionState = RETRY_CONNECT
     */
    private NextState mNextConnectionState = NextState.DISCONNECT;

    private enum NextState {
        RETRY_CONNECT(1),
        DISCONNECT(2),
        RETRY_AFTER_KICKEDOUT(3);

        private final int value;

        NextState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public WsConnection(String host, int port, Context context, Parser parser, UserConfig config) {
        super(host, port);
        this.context = context;
        this.parser = parser;
        this.userConfig = config;
//        mSocketClient = new NettyClient(host, port, new ConnectionListener());
        mSocketClient = new TCPClient(host, port, new ConnectionListener(this));
        HandlerThread thread = new HandlerThread("wsconnection");
        thread.start();
        mConnectionHandler = new Handler(thread.getLooper());

        subscribeServerPongEvent();
        subscribeKeepClientHeartBeatEvent();
        subscribeRetryConnectionEvent(context);
    }

    private void subscribeRetryConnectionEvent(Context context) {
        Subscription subscription =
                Observable.interval(TIMER_CONNECTION_CHECK, TimeUnit.SECONDS)
                        .map((value) -> mCheckCountDown--)
                        .filter((value) ->
                                !mSocketClient.isConnected() &&
                                        !mSocketClient.isConnecting() &&
                                        (mNextConnectionState == NextState.RETRY_CONNECT ||
                                                mNextConnectionState == NextState.RETRY_AFTER_KICKEDOUT) &&
                                        NetworkHelper.isNetworkAvailable(context) &&
                                        mCheckCountDown <= 0
                        )
                        .subscribe((value) -> {
                            Timber.d("Check for reconnect");
                            connect();
                        });
        compositeSubscription.add(subscription);
    }

    private void subscribeKeepClientHeartBeatEvent() {
        Subscription subscription =
                Observable.interval(TIMER_HEARTBEAT, TimeUnit.SECONDS)
                        .filter((value) -> mSocketClient.isConnected())
                        .subscribe((value) -> {
                            Timber.d("Begin send heart beat [%s]", value);
                            ping();
                        });
        compositeSubscription.add(subscription);
    }

    private void subscribeServerPongEvent() {
        mServerPongBus = new RxBus();
        Subscription subscription =
                mServerPongBus.toObserverable()
                        .filter((obj) -> mSocketClient.isConnected() && this.isUserLoggedIn())
                        .debounce(SERVER_TIMEOUT, TimeUnit.SECONDS)
                        .filter((obj) -> this.isUserLoggedIn() &&
                                (mNextConnectionState == NextState.RETRY_CONNECT))
                        .subscribe((obj) -> {
                            Timber.d("Server is not responding ...");
                            reconnect();
                        });
        compositeSubscription.add(subscription);
    }

    private boolean isUserLoggedIn() {
        if (userConfig == null) {
            return false;
        }

        if (!userConfig.hasCurrentUser()) {
            return false;
        }

        return !TextUtils.isEmpty(userConfig.getCurrentUser().zaloPayId);
    }

    public void setGCMToken(String token) {
        this.gcmToken = token;
    }

    @Override
    public void cleanup() {
        super.cleanup();

        Timber.d("Should cleanup current instance to prevent memory leak");
        if (compositeSubscription != null) {
            compositeSubscription.clear();
            compositeSubscription = null;
        }

        if (mSocketClient != null) {
            mSocketClient.disconnect();
            mSocketClient = null;
        }
    }

    @Override
    public void connect() {
        if (mConnectionHandler.getLooper() != Looper.myLooper()) {
            mConnectionHandler.post(() -> {
                Timber.d("Trigger new connection");
                mNextConnectionState = NextState.RETRY_CONNECT;
                mSocketClient.connect();
            });
        } else {
            Timber.d("Trigger new connection inside looper");
            if (mSocketClient.isConnected() || mSocketClient.isConnecting()) {
                Timber.d("Skip because connection is on the go");
                return;
            }

            mNextConnectionState = NextState.RETRY_CONNECT;
            mSocketClient.connect();
        }
    }

    @Override
    public void ping() {
        if (!isUserLoggedIn()) {
            Timber.d("User is not login. Should stop sending ping");
            return;
        }

        NotificationApiMessage pingMessage = NotificationApiHelper.createPingMessage(getCurrentUserId());
        send(pingMessage.messageCode, pingMessage.messageContent);
    }

    private void reconnect() {
        disconnect();

        mConnectionHandler.postDelayed(() -> {
            // this code will be executed after 1 seconds
            if (!NetworkHelper.isNetworkAvailable(context)) {
                Timber.d("Skip trigger new connection.");
                return;
            }

            Timber.d("Begin to reconnect");
            mNextConnectionState = NextState.RETRY_CONNECT;
            connect();
        }, 1000);
    }

    @Override
    public void disconnect() {
        Timber.d("disconnect");
        mNextConnectionState = NextState.DISCONNECT;
        doDisconnect();
    }


    private void doDisconnect() {
        if (mSocketClient != null) {
            mSocketClient.disconnect();
        }
    }

    @Override
    public boolean isConnected() {
        return mSocketClient.isConnected();
    }

    @Override
    public boolean isConnecting() {
        return mSocketClient.isConnecting();
    }

    @Override
    public boolean send(int msgType, String data) {
        return false;
    }

    @Override
    public boolean send(int msgType, byte[] data) {
        ByteBuffer bufTemp = ByteBuffer.allocate(HEADER_LENGTH + data.length);
        bufTemp.putInt(data.length + TYPE_FIELD_LENGTH);
        bufTemp.put((byte) msgType);
        bufTemp.put(data);

        mSocketClient.send(bufTemp.array());

        return true;
    }

    private boolean sendAuthentication(String token, long uid) {
        NotificationApiMessage authenticationMessage = NotificationApiHelper.createAuthenticationMessage(token, uid, gcmToken);
        return send(authenticationMessage.messageCode, authenticationMessage.messageContent);
    }

    private boolean sendAuthentication() {
        if (userConfig.hasCurrentUser()) {
            User user = userConfig.getCurrentUser();
            return sendAuthentication(user.accesstoken, Long.parseLong(user.zaloPayId));
        }
        return false;
    }

    private long getCurrentUserId() {
        long uid = -1;

        try {
            uid = Long.parseLong(userConfig.getCurrentUser().zaloPayId);
        } catch (Exception ex) {
            Timber.d(ex, "parse zaloPayId exception");
        }

        return uid;
    }

    private boolean sendFeedbackStatus(Event event) {
        try {
            long mtaid = event.getMtaid();
            long mtuid = event.getMtuid();
            long uid = getCurrentUserId();

            if (mtaid <= 0 && mtuid <= 0) {
                return true;
            }

            Timber.d("Send feedback status with mtaid %s mtuid %s zaloPayId %s", mtaid, mtuid, uid);
            NotificationApiMessage message = NotificationApiHelper.createFeedbackMessage(mtaid, mtuid, uid);
            return send(message.messageCode, message.messageContent);
        } catch (Throwable e) {
            Timber.w(e, "Exception while sending feedback message");
            return false;
        }
    }

    private void scheduleReconnect() {
        if (!userConfig.hasCurrentUser()) {
            Timber.d("Don't have signed in user. Skip reconnect.");
            return;
        }

        if (!NetworkHelper.isNetworkAvailable(context)) {
            Timber.d("Don't have network connection. Skip reconnect. When network connection is available, there is a event that will trigger new connection");
            return;
        }

        numRetry++;
        mCheckCountDown = numRetry % 10L;
        Timber.d("Try to reconnect after %s (seconds) at [%s]-th time", mCheckCountDown * TIMER_CONNECTION_CHECK, numRetry);
    }

    /**
     * Handle socket connected event
     */
    private void handleOnConnected() {
        Timber.d("onConnected");
        mState = State.Connected;
        mNextConnectionState = NextState.RETRY_CONNECT;
        //    numRetry = 0;
        sendAuthentication();
        mServerPongBus.send(1L);

        EventBus.getDefault().post(new WsConnectionEvent(WsConnectionEvent.CONNECTED));
    }

    /**
     * Handle socket received message event
     */
    private void handleOnMessage(byte[] data) {
        Timber.v("onReceived: %s bytes", data.length);

        Event message = parser.parserMessage(data);
        if (message == null) {
            return;
        }

        ServerMessageType messageType = ServerMessageType.fromValue(message.getMsgType());
        Timber.v("message.msgType %s", messageType);
        boolean needFeedback = true;

        if (messageType == ServerMessageType.AUTHEN_LOGIN_RESULT) {
            numRetry = 0;
            postResult(message);
            mServerPongBus.send(0L);
        } else if (messageType == ServerMessageType.KICK_OUT_USER) {
            needFeedback = false;
            if (mNextConnectionState != NextState.RETRY_AFTER_KICKEDOUT) {
                mNextConnectionState = NextState.RETRY_AFTER_KICKEDOUT;
            } else {
                // kicked out 1 time, this time should disconnect
                mNextConnectionState = NextState.DISCONNECT;
            }
            doDisconnect();
        } else if (messageType == ServerMessageType.PONG_CLIENT) {
            needFeedback = false;
            long currentTime = System.currentTimeMillis();
            Timber.v("Got pong from server. Time elapsed: %s", currentTime - ((ServerPongData) message).clientData);
            mServerPongBus.send(currentTime - ((ServerPongData) message).clientData);
        } else {

            if (messageType == ServerMessageType.RECOVERY_RESPONSE) {
                needFeedback = false;
            }

            postResult(message);
            mServerPongBus.send(2L);
        }

        if (needFeedback) {
            sendFeedbackStatus(message);
        }
    }

    /**
     * Handle socket disconnected event
     */
    private void handleOnDisconnected(ConnectionErrorCode code, String reason) {
        Timber.d("onDisconnected %s", code);
        mState = Connection.State.Disconnected;

        if (mSocketClient != null) {
            mSocketClient.disconnect();
        }

        Timber.d("Next expected network state: %s", mNextConnectionState);
        if (mNextConnectionState == NextState.RETRY_CONNECT) {
            scheduleReconnect();
        }
        EventBus.getDefault().post(new WsConnectionEvent(WsConnectionEvent.DISCONNECTED));
    }

    /**
     * Handle socket error event
     */
    private void handleOnError(Throwable e) {
        Timber.d("onError %s", e);
        mState = Connection.State.Disconnected;

        if (e instanceof SocketTimeoutException) {
//            } else if (e instanceof ConnectTimeoutException) {
        } else if (e instanceof ConnectException) {
        } else if (e instanceof UnknownHostException) {
        }

        if (mNextConnectionState == NextState.RETRY_CONNECT) {
            scheduleReconnect();
        }
        EventBus.getDefault().post(new WsConnectionEvent(WsConnectionEvent.DISCONNECTED));
    }

    private static class ConnectionListener implements Listener {
        private WeakReference<WsConnection> mConnection;

        ConnectionListener(WsConnection connection) {
            this.mConnection = new WeakReference<>(connection);
        }

        @Override
        public void onConnected() {
            if (mConnection.get() == null) {
                Timber.i("WsConnection is NULL when receiving onConnected event");
                return;
            }

            mConnection.get().handleOnConnected();
        }

        @Override
        public void onMessage(byte[] data) {
            if (mConnection.get() == null) {
                Timber.i("WsConnection is NULL when receiving onMessage event");
                return;
            }

            mConnection.get().handleOnMessage(data);
        }

        @Override
        public void onDisconnected(ConnectionErrorCode code, String reason) {
            if (mConnection.get() == null) {
                Timber.i("WsConnection is NULL when receiving onDisconnected event");
                return;
            }

            mConnection.get().handleOnDisconnected(code, reason);
        }

        @Override
        public void onError(Throwable e) {
            if (mConnection.get() == null) {
                Timber.i("WsConnection is NULL when receiving onError event");
                return;
            }

            mConnection.get().handleOnError(e);
        }
    }
}
