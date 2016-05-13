package vn.com.vng.zalopay;

/**
 * Created by AnhHieu on 3/25/16.
 */
public class Constants {
    public static final int CONNECTION_POOL_COUNT = 3;
    public static final long KEEP_ALIVE_DURATION_MS = 30000l;
    public static final long DEFAULT_CONNECTION_TIMEOUT_MINUTES = 2L;


    public static final String PREF_USER_SESSION = "pref_user_session";
    public static final String PREF_USER_EXPIREIN = "pref_user_expirein";

    public static final String PREF_USER_ID = "pref_user_id";
    public static final String PREF_USER_NAME = "pref_user_name";
    public static final String PREF_USER_AVATAR = "pref_user_avatar";
    public static final String PREF_USER_EMAIL = "pref_user_email";

    public static final String ARG_AMOUNT = "AMOUNT";
    public static final String ARG_PAYEE = "Payee";

    //DATA MANIFESTS DATABASE
    public static final String MANIF_BALANCE = "manif_balance";

    public static final String ORDER_INFO = "order_info";

    //DMappedCard: model of zalo payment sdk
    public static final String CARDNAME = "cardname";
    public static final String FIRST6CARDNO = "first6cardno";
    public static final String LAST4CARDNO = "last4cardno";
    public static final String BANKCODE = "bankcode";
    public static final String EXPIRETIME = "expiretime";

}
