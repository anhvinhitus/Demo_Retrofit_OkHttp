package vn.com.vng.zalopay.tracker.model;

/**
 * Created by hieuvm on 5/10/17.
 * *
 */

public abstract class AbstractLogData {

    public abstract String getMessage();

    public final int eventType;
    public final long timestamp;

    AbstractLogData(int eventType) {
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }

}
