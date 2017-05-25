package vn.com.vng.zalopay.event;

/**
 * Created by hieuvm on 5/24/17.
 * *
 */

public class AppStateChangeEvent {
    public boolean isForeground;

    public AppStateChangeEvent(boolean isForeground) {
        this.isForeground = isForeground;
    }
}
