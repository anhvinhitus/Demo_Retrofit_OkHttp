package vn.com.vng.zalopay.ui.view;

import android.content.Context;

/**
 * Created by AnhHieu on 5/13/16.
 */
public interface ISplashScreenView {

    Context getContext();

    void gotoHomeScreen();

    void gotoLoginScreen();

    void showLoading();

    void hideLoading();
}
