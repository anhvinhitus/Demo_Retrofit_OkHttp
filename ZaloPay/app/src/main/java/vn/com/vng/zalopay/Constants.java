package vn.com.vng.zalopay;

/**
 * Created by AnhHieu on 3/25/16.
 */
public interface Constants {
    int CONNECTION_POOL_DOWNLOAD_COUNT = 1;
    int CONNECTION_KEEP_ALIVE_DOWNLOAD_DURATION = 5;
    int CONNECTION_POOL_COUNT = 5;
    long CONNECTION_KEEP_ALIVE_DURATION = 5;
    int MIN_DEPOSIT_MONEY = 20000;
    int MAX_DEPOSIT_MONEY = 10000000;
    int MIN_WITHDRAW_MONEY = 20000;
    int MAX_WITHDRAW_MONEY = 10000000;
    int MIN_TRANSFER_MONEY = 10000;
    int MAX_TRANSFER_MONEY = 10000000;

    String PREF_USER_SESSION = "pref_user_session";
    String PREF_USER_EXPIREIN = "pref_user_expirein";
    String PREF_ZALO_ID = "pref_zalo_id";
    String PREF_USER_ID = "pref_user_id";
    String PREF_USER_NAME = "pref_user_name";
    String PREF_USER_AVATAR = "pref_user_avatar";
    String PREF_USER_EMAIL = "pref_user_email";
    String PREF_USER_BIRTHDAY = "pref_user_birth_date";
    String PREF_USER_GENDER = "pref_user_gender";
    String PREF_USER_PHONE = "pref_user_phone";
    String PREF_USER_IDENTITY_NUMBER = "pref_user_identity";
    String PREF_PROFILE_LEVEL = "profilelevel";
    String PREF_PROFILE_PERMISSIONS = "profilePermissions";
    String PREF_USER_LAST_USER_ID = "pref_last_uid";
    String PREF_INVITATION_USERID = "pref_userid_invitation";
    String PREF_INVITATION_SESSION = "pref_session_invitation";
    String PREF_USER_ZALOPAY_NAME = "pref_zalopay_name";

    //Using to transfer arg between fragment/activity
    String ARG_AMOUNT = "AMOUNT";
    String ARG_ZALO_FRIEND = "zalofriend";
    String ARG_MESSAGE = "message";
    String ARG_TRANSFERRECENT = "TransferRecent";
    String ARG_URL = "url";
    String ARG_AUTO_LOAD_DATA = "auto_load_data";

    //DMappedCard: model of zalo payment sdk

    String LAST4CARDNO = "last4cardno";
    String IMAGE_FILE_PATH = "image_file_path";
    String BANKNAME = "bankname";


    String PREF_WAITING_APPROVE_PROFILE_LEVEL3 = "pref_waiting_approve_profile_level3";

    int ZALOPAY_APP_ID = BuildConfig.ZALOPAY_APP_ID;
    int REQUEST_CODE_TRANSFER = 124;
    //    int REQUEST_CODE_INTRO = 125;
    int REQUEST_CODE_CARD_SUPPORT = 126;
    int REQUEST_CODE_TRANSFER_VIA_ZALOPAYID = 127;
    int REQUEST_CODE_DEPOSIT = 128;
    int REQUEST_CODE_UPDATE_PROFILE_LEVEL_2 = 129;
    int REQUEST_CODE_SYSTEM_SETTINGS = 130;

    String ARG_MONEY_TRANSFER_MODE = "transferMode";
    String ARG_SHOW_NOTIFICATION_LINK_CARD = "show_notification_link_card";

    interface MoneyTransfer {
        int MODE_DEFAULT = 0;
        int MODE_QR = 1;
        int MODE_ZALO = 2;
        int STAGE_PRETRANSFER = 1;
        int STAGE_TRANSFER_SUCCEEDED = 2;
        int STAGE_TRANSFER_FAILED = 3;
        int STAGE_TRANSFER_CANCEL = 4;
    }

 /*   public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";*/

    interface QRCode {
        int RECEIVE_MONEY = 1;
    }
}
