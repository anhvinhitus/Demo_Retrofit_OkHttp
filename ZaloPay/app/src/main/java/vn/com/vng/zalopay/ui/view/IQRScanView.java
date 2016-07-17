package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

/**
 * Created by longlv on 09/05/2016.
 */
public interface IQRScanView extends ILoadDataView {
    Activity getActivity();
    void onTokenInvalid();
    void resumeScanner();
}
