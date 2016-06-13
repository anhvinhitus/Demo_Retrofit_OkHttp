package vn.com.vng.zalopay.service;

import android.app.Activity;

import timber.log.Timber;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by huuhoa on 6/11/16.
 * Implementation for global event handling service
 */
public class GlobalEventHandlingServiceImpl implements GlobalEventHandlingService {
    private Activity activity;
    @Override
    public void setMainActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void showMessage(int messageType, String title, String body) {
        if (this.activity == null) {
            Timber.w("Global activity is null");
            return;
        }

        Timber.d(body);
        new SweetAlertDialog(this.activity, messageType).setContentText(body)
            .show();
    }
}
