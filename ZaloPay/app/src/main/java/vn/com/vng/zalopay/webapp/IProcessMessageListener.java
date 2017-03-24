package vn.com.vng.zalopay.webapp;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by khattn on 2/20/17.
 * Declare interface for web view listener
 */
interface IProcessMessageListener {
    void payOrder(JSONObject jsonObject, IPaymentListener listener);

    void logout();

    void showDialog(int dialogType, String title, String message, String buttonLabel);

    void showLoading();

    void hideLoading();

    Context getContext();
}