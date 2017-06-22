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
    public static final int PREFERENTIAL = 118;

    public static boolean shouldMarkRead(long notificationType) {
        return notificationType == ORDER_PAYMENT || notificationType == APP_P2P_NOTIFICATION || notificationType == PROMOTION;
    }

    public static boolean needReloadBalanceAndTransaction(int notificationType) {
        switch (notificationType) {
            /*case NotificationType.ORDER_PAYMENT://Reload balance & transaction when handle callback from PaymentSDK
            case NotificationType.TOPUP_WALLET://Reload balance & transaction when handle callback from PaymentSDK
            case NotificationType.ADD_BANK_CARD://Reload balance & transaction when handle callback from PaymentSDK
            case NotificationType.MONEY_WITHDRAW://Reload balance & transaction when handle callback from PaymentSDK
            case NotificationType.RECEIVE_RED_PACKET:
            case NotificationType.REFUND_RED_PACKET:
            case NotificationType.DONATE_MONEY:
            case NotificationType.REFUND_TRANSACTION:
            case NotificationType.RETRY_TRANSACTION: //Giao dịch của bạn đã retry thành công
            case NotificationType.REFUND_TRANSACTION_BANK: //Yêu cầu hoàn tiền đang được ngân hàng xử lý
            case NotificationType.RECOVERY_MONEY:
            case NotificationType.MERCHANT_TRANSFER:
            case NotificationType.DEPOSIT_FROM_WEB_VCB_SUCCESS:
            case NotificationType.MONEY_TRANSFER:
            case NotificationType.PROMOTION://need reload balance and transaction history on promotion (cashback)
                return true;*/
            case NotificationType.UPDATE_PROFILE_LEVEL_OK: //Notification Profile -> don't need reload
            case NotificationType.UPDATE_PROFILE_LEVEL_FAILED:
            case NotificationType.UPLOAD_PROFILE_LEVEL_3:
            case NotificationType.APP_P2P_NOTIFICATION://Balance & transaction not change
            case NotificationType.RESET_PAYMENT_PASSWORD://Balance & transaction not change
            case NotificationType.NOTIFICATION_RECEIVE_RED_PACKET://Reloaded when receive red packet
            case NotificationType.LINK_CARD_EXPIRED://Balance & transaction not change
            case NotificationType.MERCHANT_BILL:
            case NotificationType.LINK_ACCOUNT: //Balance & transaction not change
            case NotificationType.UNLINK_ACCOUNT://Balance & transaction not change
                return false;
            default:
                return true;
        }
    }

}

