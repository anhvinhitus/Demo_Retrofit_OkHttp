package vn.com.vng.zalopay.ui.subscribe;

import android.app.Activity;

import java.lang.ref.WeakReference;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 12/14/16.
 * *
 */

public class StartPaymentAppSubscriber extends DefaultSubscriber<Boolean> {
    private Navigator mNavigator;
    private WeakReference<Activity> mActivity;
    private AppResource app;

    public StartPaymentAppSubscriber(Navigator navigator, Activity activity, AppResource app) {
        this.mNavigator = navigator;
        this.mActivity = new WeakReference<>(activity);
        this.app = app;
    }

    @Override
    public void onNext(Boolean result) {
        if (mActivity == null || mActivity.get() == null) {
            return;
        }

        if (result) {
            mNavigator.startPaymentApplicationActivity(mActivity.get(), app);
        } else {
            DialogHelper.showCustomDialog(mActivity.get(),
                    mActivity.get().getString(R.string.application_downloading),
                    mActivity.get().getString(R.string.txt_close),
                    SweetAlertDialog.NORMAL_TYPE, null);
        }
    }
}