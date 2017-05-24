package vn.com.vng.zalopay.tracker.model;


import java.util.Locale;

import static vn.com.vng.zalopay.tracker.model.TrackerType.PAYMENT_CONNECTOR_ERROR_TYPE;

/**
 * Created by hieuvm on 5/10/17.
 * *
 */

public class PaymentConnectorErrorLogData extends AbstractLogData {
    // eventType \t currentUid \t receivedUid \t mtuid \t sourceid \t timestamp
    private static final String MSG_FORMAT = "%s\t%s\t%s\t%s\t%s\t%s";

    private final String currentUid;
    private final String receivedUid;
    private final long mtuid;
    private final int sourceid;
    private final long timestamp;

    public PaymentConnectorErrorLogData(String currentUid, String receivedUid, long mtuid, int sourceid, long timestamp) {
        super(PAYMENT_CONNECTOR_ERROR_TYPE);
        this.currentUid = currentUid;
        this.receivedUid = receivedUid;
        this.mtuid = mtuid;
        this.sourceid = sourceid;
        this.timestamp = timestamp;
    }

    @Override
    public String getMessage() {
        return String.format(Locale.getDefault(), MSG_FORMAT, eventType, currentUid, receivedUid, mtuid, sourceid, timestamp);
    }
}
