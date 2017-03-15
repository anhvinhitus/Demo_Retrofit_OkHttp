package vn.com.vng.zalopay.domain;

/**
 * Created by longlv on 17/05/2016.
 * *
 */
public interface Constants {

    /* Object Order START */
    String APPID = "appid";
    String APPTRANSID = "apptransid";
    String ZPTRANSTOKEN = "zptranstoken";
    String APPUSER = "appuser";
    String ZALOPAYACCESSTOKEN = "zalopayaccesstoken";
    String ZALOUSERID = "zalouserid";
    String APPTIME = "apptime";
    String AMOUNT = "amount";
    String ITEM = "item";
    String DESCRIPTION = "description";
    String EMBEDDATA = "embeddata";
    String MAC = "mac";
    String CHARGEINFO = "chargeinfo";
    /* Object Order END */

    //BundleName
    String WALLETTRANSID = "walletTransID";

    interface ProfileLevel2 {
        String PROFILE_LEVEL2 = "profile_level2";
        String PHONE_NUMBER = "phone_number";
        String RECEIVE_OTP = "receive_otp";
        String TIME_RECEIVE_OTP = "time_receive_otp";
    }

    interface ChangePin {
        String CHANGE_PIN = "change_pin";
        String RECEIVE_OTP_KEY = "receive_otp_key";
        String TIME_RECEIVE_OTP_KEY = "time_receive_otp_key";
    }
}