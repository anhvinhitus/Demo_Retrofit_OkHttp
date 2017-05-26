package vn.com.vng.zalopay.notification;

/**
 * Created by huuhoa on 7/23/16.
 * Constants for notification type
 * Ref: https://docs.google.com/spreadsheets/d/1aEQ1BxVLIJT24podXtZTFh0026YYbn_6Fx4mAeOeEfQ/edit#gid=0
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
    public static final int RECOVERY_MONEY = 10;
    public static final int MERCHANT_TRANSFER = 11;
    public static final int DEPOSIT_FROM_WEB_VCB_SUCCESS = 12;

    public static final int UPDATE_PROFILE_LEVEL_OK = 100;
    public static final int PAYMENT_APP_PROMOTION = 101;
    public static final int UPDATE_PROFILE_LEVEL_FAILED = 102;
    public static final int SEND_RED_PACKET = 103;
    public static final int UPLOAD_PROFILE_LEVEL_3 = 104;
    public static final int REFUND_TRANSACTION = 105;
    public static final int REFUND_TRANSACTION_BANK = 106;
    public static final int RETRY_TRANSACTION = 107;
    public static final int NOTIFICATION_MERCHANT_APP = 108;
    public static final int APP_P2P_NOTIFICATION = 109;
    public static final int RESET_PAYMENT_PASSWORD = 110;
    public static final int NOTIFICATION_RECEIVE_RED_PACKET = 111;

    public static final int LINK_CARD_EXPIRED = 112;
    public static final int MERCHANT_BILL = 113; //Thanh toán đơn hàng
    public static final int NOTIFICATION_ALL_USER = 114;
    public static final int UNLINK_ACCOUNT = 115;
    public static final int LINK_ACCOUNT = 116;
    public static final int PROMOTION = 117;

    static boolean shouldMarkRead(long notificationType) {
        return notificationType == ORDER_PAYMENT || notificationType == APP_P2P_NOTIFICATION || notificationType == PROMOTION;
    }


}

