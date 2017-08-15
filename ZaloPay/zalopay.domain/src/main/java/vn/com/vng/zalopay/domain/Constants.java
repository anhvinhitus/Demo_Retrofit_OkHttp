package vn.com.vng.zalopay.domain;

import java.util.HashMap;
import java.util.Map;

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

    String TRANSTOKEN = "zptranstoken";
    /* Object Order END */

    interface ChangePin {
        String CHANGE_PIN = "change_pin";
        String RECEIVE_OTP_KEY = "receive_otp_key";
        String TIME_RECEIVE_OTP_KEY = "time_receive_otp_key";
    }

    interface TransferMoneyWebAPI {
        String ZPID = "zpid";
        String MESSAGE = "message";
        String AMOUNT = "amount";
    }
}
