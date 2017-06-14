package vn.com.vng.zalopay.webapp;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by khattn on 2/20/17.
 * Declare interface for web view listener
 */
interface IProcessMessageListener {
    void payOrder(JSONObject jsonObject, IPaymentListener listener);

    void transferMoney(JSONObject jsonObject, IPaymentListener listener);

    void showDialog(int dialogType, String title, String message, String buttonLabel);

    void showLoading();

    void hideLoading();

    void writeLog(String type, long time, String data);

    void launchApp(String packageID, String alternateUrl);

    void launchInternalApp(int internalAppID);

    void sendEmail();

    void openDial();
}
