package vn.com.vng.zalopay.event;

/**
 * Created by huuhoa on 11/25/16.
 * Event for Zalo integration
 */
public class ZaloIntegrationEvent {
    public final EventType eventType;
    public final long receiverId;
    public ZaloIntegrationEvent(EventType eventType, long receiverId) {
        this.eventType = eventType;
        this.receiverId = receiverId;
    }

    public enum EventType {
        SEND_MONEY,
        SEND_RED_PACKET,
        VIEW_TRANSACTION
    }
}
