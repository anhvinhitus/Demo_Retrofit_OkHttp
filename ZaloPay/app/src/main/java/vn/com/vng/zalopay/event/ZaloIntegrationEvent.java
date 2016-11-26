package vn.com.vng.zalopay.event;

/**
 * Created by huuhoa on 11/25/16.
 * Event for Zalo integration
 */
public class ZaloIntegrationEvent {
    public final EventType eventType;
    public final long receiverId;
    public final String receiverName;
    public final String receiverAvatar;
    public ZaloIntegrationEvent(EventType eventType, long receiverId, String receiverName, String receiverAvatar) {
        this.eventType = eventType;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.receiverAvatar = receiverAvatar;
    }

    public enum EventType {
        SEND_MONEY,
        SEND_RED_PACKET,
        VIEW_TRANSACTION
    }
}
