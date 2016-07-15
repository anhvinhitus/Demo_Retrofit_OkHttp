package vn.com.vng.zalopay.data.ws.connection;

import com.google.protobuf.AbstractMessage;

/**
 * Created by AnhHieu on 6/14/16.
 */
public abstract class Connection {

    protected State mState = State.Disconnected;

    public enum State {
        Disconnected,
        Connecting,
        Connected
    }

    public abstract void connect();

    public abstract void ping();

    public abstract void disconnect();

    public abstract boolean send(int msgType, String data);

    public abstract boolean send(int msgType, byte[] data);

    public abstract boolean send(int msgType, AbstractMessage msgData);

    public boolean isConnected() {
        return mState == State.Connected;
    }

    public boolean isConnecting(){
        return mState == State.Connecting;
    }

    public State getState() {
        return mState;
    }
}
