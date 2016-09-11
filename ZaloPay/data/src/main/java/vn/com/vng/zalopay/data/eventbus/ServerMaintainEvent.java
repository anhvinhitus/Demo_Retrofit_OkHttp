package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by huuhoa on 6/22/16.
 * Event for reporting that server is in maintain mode
 */
public class ServerMaintainEvent {
    private final String mMessage;

    public ServerMaintainEvent(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }
}
