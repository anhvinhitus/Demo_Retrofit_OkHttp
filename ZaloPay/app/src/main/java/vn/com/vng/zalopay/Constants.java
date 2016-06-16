package vn.com.vng.zalopay;

/**
 * Created by AnhHieu on 3/25/16.
 */
public class Constants {
    public static final int PROFILE_LEVEL_MIN = 2;
    public static final int CONNECTION_POOL_COUNT = 3;
    public static final long KEEP_ALIVE_DURATION_MS = 30000l;
    public static final long DEFAULT_CONNECTION_TIMEOUT_MINUTES = 2L;


    public static final String PREF_USER_SESSION = "pref_user_session";
    public static final String PREF_USER_EXPIREIN = "pref_user_expirein";

    public static final String PREF_ZALO_ID = "pref_zalo_id";
    public static final String PREF_USER_ID = "pref_user_id";
    public static final String PREF_USER_NAME = "pref_user_name";
    public static final String PREF_USER_AVATAR = "pref_user_avatar";
    public static final String PREF_USER_EMAIL = "pref_user_email";
    public static final String PREF_USER_BIRTHDATE = "pref_user_birth_date";
    public static final String PREF_USER_GENDER = "pref_user_gender";
    public static final String PREF_PROFILELEVEL = "profilelevel";
    public static final String PREF_PROFILEPERMISSIONS = "profilePermisssions";

    public static final String ARG_AMOUNT = "AMOUNT";
    public static final String ARG_PAYEE = "Payee";
    public static final String ARG_ZALO_FRIEND = "zalofriend";
    public static final String ARG_MESSAGE = "message";
    public static final String ARG_TRANSFERRECENT = "TransferRecent";

    //DATA MANIFESTS DATABASE
    public static final String MANIF_BALANCE = "manif_balance";

    public static final String ORDER_INFO = "order_info";

    //DMappedCard: model of zalo payment sdk
    public static final String CARDNAME = "cardname";
    public static final String FIRST6CARDNO = "first6cardno";
    public static final String LAST4CARDNO = "last4cardno";
    public static final String BANKCODE = "bankcode";
    public static final String EXPIRETIME = "expiretime";

    public static final String PROFILE_TYPE = "profile_type";
    public static final int PRE_PROFILE_TYPE = 1;
    public static final int PIN_PROFILE_TYPE = 2;

    public static final int REQUEST_CODE_TRANSFER = 124;


    public static class ModuleName {
        public static final String NOTIFICATIONS = "Notifications";
        public static final String PAYMENT_MAIN = "PaymentMain";
        public static final String ABOUT = "About";
        public static final String HELP = "Help";
        public static final String FAQ = "FAQ";
        public static final String TRANSACTIONLOGS = "TransactionLogs";
    }
}
