package vn.com.vng.zalopay.service;

import android.app.Activity;

/**
 * Created by huuhoa on 6/11/16.
 * Interface for handing global event
 */
public interface GlobalEventHandlingService {
    //! Initialize service with main application activity
    void setMainActivity(Activity activity);

    //! Show message box at home
    void showMessage(int messageType, String title, String body);
}
