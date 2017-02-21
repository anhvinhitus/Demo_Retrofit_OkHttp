package vn.com.vng.zalopay.service;

import android.app.Activity;
import android.support.v4.app.Fragment;

import timber.log.Timber;
import vn.com.vng.zalopay.navigation.INavigator;

/**
 * Created by longlv on 2/10/17.
 * *
 */

public abstract class DefaultPaymentRedirectListener implements PaymentWrapper.IRedirectListener {
    private INavigator mNavigator;

    public abstract Object getContext();

    public DefaultPaymentRedirectListener(INavigator navigator) {
        mNavigator = navigator;
    }

    @Override
    public void startUpdateProfileLevel(String walletTransId) {
        Object context = getContext();
        Timber.d("startUpdateProfileLevel transId[%s] context [%s] navigator[%s]",
                walletTransId, context, mNavigator);
        if (context == null || mNavigator == null) {
            return;
        }
        if (context instanceof Fragment) {
            mNavigator.startUpdateProfile2ForResult((Fragment) context, walletTransId);
        } else if (context instanceof Activity) {
            mNavigator.startUpdateProfile2ForResult((Activity) context, walletTransId);
        }
    }

    @Override
    public void startDepositForResult() {
        Object context = getContext();
        Timber.d("startDepositForResult context [%s] navigator[%s]", context, mNavigator);
        if (context == null || mNavigator == null) {
            return;
        }
        if (context instanceof Fragment) {
            mNavigator.startDepositForResultActivity((Fragment) context);
        } else if (context instanceof Activity) {
            mNavigator.startDepositForResultActivity((Activity) context);
        }
    }
}
