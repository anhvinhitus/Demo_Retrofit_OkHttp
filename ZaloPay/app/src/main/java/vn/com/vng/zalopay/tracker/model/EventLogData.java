package vn.com.vng.zalopay.tracker.model;

import java.util.Locale;

/**
 * Created by hieuvm on 5/10/17.
 * *
 */

public class EventLogData extends AbstractLogData {

    private static final String MSG_FORMAT = "%s\t%s\t%s\t%s";

    public final int eventId;
    public final Long value;

    public EventLogData(int eventType, int eventId, Long value) {
        super(eventType);
        this.eventId = eventId;
        this.value = value;
    }

    @Override
    public String getMessage() {
        return String.format(Locale.getDefault(), MSG_FORMAT, eventType, eventId, value == null ? 0 : value, timestamp);
    }
}
