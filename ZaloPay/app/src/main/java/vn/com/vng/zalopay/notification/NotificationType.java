package vn.com.vng.zalopay.notification;

/**
 * Created by huuhoa on 7/23/16.
 * Constants for notification type
 */
public class NotificationType {

    public static final int ORDER_PAYMENT = 1;
    public static final int TOPUP_WALLET = 2;
    public static final int ADD_BANK_CARD = 3;
    public static final int MONEY_TRANSFER = 4;
    public static final int MONEY_WITHDRAW = 5;
    public static final int RECEIVE_RED_PACKET = 6;
    public static final int REFUND_RED_PACKET = 7;
    public static final int DONATE_MONEY = 9;
    public static final int UPDATE_PROFILE_LEVEL_OK = 100;
    public static final int PAYMENT_APP_PROMOTION = 101;
    public static final int UPDATE_PROFILE_LEVEL_FAILED = 102;
    public static final int SEND_RED_PACKET = 103;
    public static final int UPLOAD_PROFILE_LEVEL_3 = 104;
    public static final int REFUND_TRANSACTION = 105;
    public static final int REFUND_TRANSACTION_BANK = 106;
    public static final int RETRY_TRANSACTION = 107;
    public static final int APP_P2P_NOTIFICATION = 109;
    public static final int UPDATE_PLATFORMINFO = 110;
    public static final int NOTIFICATION_RECEIVE_RED_PACKET = 111;
    public static final int RECOVERY_MONEY = 10;


    static boolean isTransactionNotification(int notificationType) {
        return notificationType == ORDER_PAYMENT ||
                notificationType == TOPUP_WALLET ||
                notificationType == ADD_BANK_CARD ||
                notificationType == MONEY_TRANSFER ||
                notificationType == MONEY_WITHDRAW ||
                notificationType == RECEIVE_RED_PACKET ||
                notificationType == REFUND_RED_PACKET ||
                notificationType == REFUND_TRANSACTION ||
                notificationType == RETRY_TRANSACTION ||
                notificationType == REFUND_TRANSACTION_BANK ||
                notificationType == DONATE_MONEY
                ;
    }

    static boolean isProfileNotification(int notificationType) {
        return notificationType == UPDATE_PROFILE_LEVEL_OK
                || notificationType == UPLOAD_PROFILE_LEVEL_3
                || notificationType == UPDATE_PROFILE_LEVEL_FAILED;
    }

    static boolean isRedPacket(int notificationType) {
        return notificationType == SEND_RED_PACKET;
    }

    static boolean shouldMarkRead(int notificationType) {
        return notificationType == ORDER_PAYMENT || notificationType == DONATE_MONEY;
    }


}
