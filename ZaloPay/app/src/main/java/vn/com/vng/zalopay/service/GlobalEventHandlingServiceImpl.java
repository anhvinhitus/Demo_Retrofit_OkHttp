package vn.com.vng.zalopay.service;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.react.bridge.JSApplicationCausedNativeException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ExecutionException;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.eventbus.ServerMaintainEvent;
import vn.com.vng.zalopay.event.InternalAppExceptionEvent;
import vn.com.vng.zalopay.event.PaymentAppExceptionEvent;
import vn.com.vng.zalopay.event.UncaughtRuntimeExceptionEvent;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by huuhoa on 6/11/16.
 * Implementation for global event handling service
 */
public class GlobalEventHandlingServiceImpl implements GlobalEventHandlingService {
    private Message mCurrentMessage;
    private Message mCurrentMessageAtLogin;
    private final EventBus mEventBus;

    public GlobalEventHandlingServiceImpl(EventBus eventBus) {
        this.mEventBus = eventBus;
        this.mEventBus.register(this);
    }

    @Override
    protected void finalize() throws Throwable {
        this.mEventBus.unregister(this);
        super.finalize();
    }

    @Override
    public void enqueueMessage(int messageType, String title, String body) {
        mCurrentMessage = new Message(messageType, title, body);
    }

    @Override
    public void enqueueMessageAtLogin(int messageType, String title, String body) {
        mCurrentMessageAtLogin = new Message(messageType, title, body);
    }

    @Override
    public void enqueueMessageAtLogin(int messageType, int title, int body) {
        mCurrentMessageAtLogin = new Message(messageType, title, body);
    }

    @Nullable
    @Override
    public Message popMessage() {
        Message message = mCurrentMessage;
        mCurrentMessage = null;
        return message;
    }

    @Override
    public Message popMessageAtLogin() {
        Message message = mCurrentMessageAtLogin;
        mCurrentMessageAtLogin = null;
        return message;
    }

    @Subscribe
    public void onInternalAppException(InternalAppExceptionEvent event) {
        enqueueMessage(SweetAlertDialog.WARNING_TYPE, "ĐÓNG", "Có lỗi xảy ra trong quá trình thực thi ứng dụng.");
        Crashlytics.log(Log.ERROR, "EXCEPTION", String.format("Internal App causes exception: %s", event.getInnerException().getMessage()));
        Crashlytics.logException(event.getInnerException());
        Answers.getInstance().logCustom(new CustomEvent("EXCEPTION INTERNAL APP"));
    }

    @Subscribe
    public void onPaymentAppException(PaymentAppExceptionEvent event) {
        enqueueMessage(SweetAlertDialog.WARNING_TYPE, "ĐÓNG", "Có lỗi xảy ra trong quá trình thực thi ứng dụng.");
        Crashlytics.log(Log.ERROR, "EXCEPTION", String.format("Payment App %d causes exception: %s", event.getAppId(), event.getInnerException().getMessage()));
        Crashlytics.logException(event.getInnerException());
        Answers.getInstance().logCustom(new CustomEvent("EXCEPTION APP " + String.valueOf(event.getAppId())));
    }

    @Subscribe
    public void onServerMaintainEvent(ServerMaintainEvent event) {
        String message;
        if (TextUtils.isEmpty(event.getMessage())) {
            message = AndroidApplication.instance().getString(R.string.exception_server_maintain);
        } else {
            message = event.getMessage();
        }
        String close = AndroidApplication.instance().getString(R.string.accept);
        enqueueMessageAtLogin(SweetAlertDialog.NORMAL_TYPE, close, message);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Timber.i("Exception with type: %s", ex.getClass());
        if (ex instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException) ex;

            Throwable cause = runtimeException.getCause();
            Timber.i("Exception cause: %s", cause == null ? "NULL" : cause.getClass());
            boolean shouldHandleException = false;
            if (cause instanceof ExecutionException || cause instanceof InterruptedException || cause == null) {
                Timber.i("Should handle uncaught exception");
                shouldHandleException = true;
            }

            if (ex instanceof JSApplicationCausedNativeException) {
                Timber.i("Should handle JSApplicationCausedNativeException exception");
                shouldHandleException = true;
            }

            if (shouldHandleException) {
                mEventBus.post(new UncaughtRuntimeExceptionEvent(ex));
            }
        }
        Timber.e(ex, "UncaughtException!!!");
    }
}
