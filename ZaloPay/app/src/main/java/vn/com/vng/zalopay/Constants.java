package vn.com.vng.zalopay;

/**
 * Created by AnhHieu on 3/25/16.
 * *
 */
public interface Constants {

    String ZPTRANSTOKEN = "zptranstoken";
    String APPID = "appid";

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
    int MAX_NUMBER_OF_TIMES_WRONG_PASS = 3;

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
    String PREF_USER_ZALOPAY_NAME = "pref_zalopay_name";

    //Using to transfer arg between fragment/activity
    String ARG_AMOUNT = "AMOUNT";
    String ARG_ZALO_FRIEND = "zalofriend";
    String ARG_MESSAGE = "message";
    String ARG_TRANSFERRECENT = "TransferRecent";
    String ARG_URL = "url";
    String ARG_LINK_BANK_TYPE = "link_bank_type";
    String ARG_BANK_LIST = "auto_load_data";
    String ARG_BANK = "bank";
    String ARG_LINK_CARD_WITH_BANK_CODE = "link_card_with_bank_code";
    String ARG_LINK_ACCOUNT_WITH_BANK_CODE = "link_account_with_bank_code";
    String ARG_GOTO_SELECT_BANK_IN_LINK_BANK = "goto_select_bank_in_link_bank";
    String ARG_CONTINUE_PAY_AFTER_LINK_BANK = "continue_pay_after_link_bank";
    String ARG_CONTINUE_WITHDRAW_AFTER_LINK_BANK = "continue_withdraw_after_link_bank";
    String ARG_UPDATE_PROFILE2_AND_LINK_ACC = "link_acc_after_update_profile2";

    int RESULT_END_PAYMENT = -2;

    //MapCard: model of zalo payment sdk

    String LAST4CARDNO = "last4cardno";
    String IMAGE_FILE_PATH = "image_file_path";
    String BANKNAME = "bankname";

    String KEY_ALIAS_NAME = "@zalopay";
    String PREF_KEY_PASSWORD = "EncryptedPassword";
    String PREF_KEY_PASSWORD_IV = "EncryptedPasswordIV";
    String PREF_USE_FINGERPRINT = "pref_use_fingerprint_to_authenticate_key";
    String PREF_USE_PROTECT_PROFILE = "pref_use_protect_profile";

    String PREF_WAITING_APPROVE_PROFILE_LEVEL3 = "pref_waiting_approve_profile_level3";
    String PREF_LAST_TIME_SHOW_FINGERPRINT_SUGGEST = "pref_time_fingerprint_suggest";
    String PREF_SHOW_FINGERPRINT_SUGGEST = "pref_show_fingerprint_suggest";

    int ZALOPAY_APP_ID = BuildConfig.ZALOPAY_APP_ID;
    int REQUEST_CODE_TRANSFER = 124;
    //    int REQUEST_CODE_INTRO = 125;
    int REQUEST_CODE_CARD_SUPPORT = 126;
    int REQUEST_CODE_TRANSFER_VIA_ZALOPAYID = 127;
    int REQUEST_CODE_DEPOSIT = 128;
    int REQUEST_CODE_SYSTEM_SETTINGS = 130;
    int REQUEST_CODE_BANK_DIALOG = 131;
    int REQUEST_CODE_LINK_BANK = 132;
    int REQUEST_CODE_UPDATE_PROFILE_LEVEL_BEFORE_LINK_ACC = 133;
    int REQUEST_CODE_SELECT_BANK = 134;

    String ARG_MONEY_TRANSFER_MODE = "transferMode";
    String ARG_MONEY_ACTIVATE_SOURCE = "activateSource";

    // Link bank tab state
    String PREF_LINK_BANK_LAST_INDEX = "pref_link_bank_last_index";
    String BANK_DATA_RESULT_AFTER_LINK = "bank_data_after_link";

    // Bank result status
    int RESULT_DO_LINK_CARD = 10;
    int RESULT_DO_LINK_ACCOUNT = 11;

    String ARGUMENT_KEY_ZALOPROFILE = "zaloprofile";
    String ARGUMENT_KEY_OAUTHTOKEN = "oauthtoken";
    String ARGUMENT_KEY_TRANSFER = "transfer";

    String TRANSFER_MODE = "transfermode";

    interface MoneyTransfer {
        int STAGE_PRETRANSFER = 1;
        int STAGE_TRANSFER_SUCCEEDED = 2;
        int STAGE_TRANSFER_FAILED = 3;
        int STAGE_TRANSFER_CANCEL = 4;
    }

 /*   public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";*/

    interface LinkBank {
        int LINK_CARD = 1;
        int LINK_ACCOUNT = 2;
    }

    enum ActivateSource {
        FromTransferActivity,
        FromQRCodeType1,
        FromQRCodeType2,
        FromZalo,
        FromWebApp_QRType2,
    }

    interface QRCode {
        String APP = "app";
        String ZALO_PAY = "ZP";
        int RECEIVE_MONEY = 1;
        int RECEIVE_FIXED_MONEY = 2;
    }

    interface ReceiveMoney {
        String TYPE = "type";
        String UID = "uid";
        String AMOUNT = "amount";
        String MESSAGE = "message";
        String CHECKSUM = "checksum";
    }

    interface TransferFixedMoney {
        String ZALO_PAY_VERSION = "zv";
        String ZALO_PAY_ID = "zpid";
        String TYPE = "t";
        String AMOUNT = "a";
        String MESSAGE = "m";
    }

    String FILE_PNG = ".png";

    interface UserAgent {
        String ZALO_PAY_CLIENT = "ZaloPayClient/";
        String PLATFORM = "Platform/android";
        String OS = "OS/";
        String SECURED = "Secured/";
    }

}
