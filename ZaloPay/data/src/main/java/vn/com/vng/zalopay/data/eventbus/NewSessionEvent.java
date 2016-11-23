package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by hieuvm on 11/23/16.
 */

public class NewSessionEvent {
    public String newSession;

    public NewSessionEvent(String newSession) {
        this.newSession = newSession;
    }
}
