package vn.com.vng.zalopay.webapp;

import android.content.Context;

import org.json.JSONObject;

import vn.com.vng.zalopay.service.PaymentWrapper;

/**
 * Created by huuhoa on 2/9/17.
 * Declare interface for web view listener
 */
interface IWebViewListener {
    void pay(JSONObject jsonObject, PaymentWrapper.IResponseListener listener);

    void payOrder(String url);

    void logout();

    void finishActivity();

    void showError(int errorCode);

    void showLoading();

    void hideLoading();

    void showDialog(int dialogType, String title, String message, String buttonLabel);

    Context getContext();
}
