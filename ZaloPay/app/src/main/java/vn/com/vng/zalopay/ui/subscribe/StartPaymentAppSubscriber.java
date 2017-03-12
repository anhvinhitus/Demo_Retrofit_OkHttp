package vn.com.vng.zalopay.ui.subscribe;

import android.app.Activity;

import java.lang.ref.WeakReference;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 12/14/16.
 * Launch payment app if app resources are already downloaded.
 * Else show dialog to let user know that app resources are downloading
 */

public class StartPaymentAppSubscriber extends DefaultSubscriber<Boolean> {
    private Navigator mNavigator;
    private WeakReference<Activity> mActivity;
    private AppResource mAppResource;

    public StartPaymentAppSubscriber(Navigator navigator, Activity activity, AppResource appResource) {
        this.mNavigator = navigator;
        this.mActivity = new WeakReference<>(activity);
        this.mAppResource = appResource;
    }

    @Override
    public void onNext(Boolean isAppAvailable) {
        if (mActivity == null || mActivity.get() == null) {
            return;
        }

        if (isAppAvailable) {
            mNavigator.startPaymentApplicationActivity(mActivity.get(), mAppResource);
        } else {
            DialogHelper.showCustomDialog(mActivity.get(),
                    mActivity.get().getString(R.string.application_downloading),
                    mActivity.get().getString(R.string.txt_close),
                    SweetAlertDialog.NORMAL_TYPE, null);
        }
    }
}
