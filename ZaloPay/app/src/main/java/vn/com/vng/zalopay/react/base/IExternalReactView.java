package vn.com.vng.zalopay.react.base;

import android.app.Activity;

/**
 * Created by longlv on 3/13/17.
 * *
 */

interface IExternalReactView {

    Activity getActivity();

    void startReactApplication();

    void showWaitingDownloadApp();
}
