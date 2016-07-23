package vn.com.vng.zalopay.notification;

/**
 * Created by huuhoa on 7/23/16.
 * Constants for notification type
 */
class NotificationType {
    public static final int ORDER_PAYMENT = 1;
    public static final int TOPUP_WALLET = 2;
    public static final int ADD_BANK_CARD = 3;
    public static final int MONEY_TRANSFER = 4;
    public static final int MONEY_WITHDRAW = 5;
    public static final int RECEIVE_RED_PACKET = 6;
    public static final int REFUND_RED_PACKET = 7;
    public static final int UPDATE_PROFILE_LEVEL_OK = 100;
    public static final int PAYMENT_APP_PROMOTION = 101;
    public static final int UPDATE_PROFILE_LEVEL_FAILED = 102;
    public static final int SEND_RED_PACKET = 103;
    public static final int UPLOAD_PROFILE_LEVEL_3 = 104;
    public static final int REFUND_TRANSACTION = 105;

    static boolean isTransactionNotification(int notificationType) {
        return notificationType == ORDER_PAYMENT ||
                notificationType == TOPUP_WALLET ||
                notificationType == ADD_BANK_CARD ||
                notificationType == MONEY_TRANSFER ||
                notificationType == MONEY_WITHDRAW ||
                notificationType == RECEIVE_RED_PACKET ||
                notificationType == REFUND_RED_PACKET ||
                notificationType == REFUND_TRANSACTION ||
                notificationType == SEND_RED_PACKET;
    }
}
