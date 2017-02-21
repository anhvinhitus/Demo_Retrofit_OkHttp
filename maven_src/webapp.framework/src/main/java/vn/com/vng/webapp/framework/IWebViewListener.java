package vn.com.vng.webapp.framework;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by huuhoa on 2/9/17.
 * Declare interface for web view listener
 */
public interface IWebViewListener {
    void finishActivity();

    void showError(int errorCode);

    void showLoading();

    void hideLoading();

    Context getContext();

    void onReceivedTitle(String title);
}
