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
    public void startUpdateProfileLevel() {
        Object context = getContext();
        if (context == null || mNavigator == null) {
            return;
        }
        if (context instanceof Fragment) {
            mNavigator.startUpdateProfile2ForResult((Fragment) context);
        } else if (context instanceof Activity) {
            mNavigator.startUpdateProfile2ForResult((Activity) context);
        }
    }

    @Override
    public void startDepositForResult() {
        Object context = getContext();
        if (context == null || mNavigator == null) {
            return;
        }
        if (context instanceof Fragment) {
            mNavigator.startDepositForResultActivity((Fragment) context);
        } else if (context instanceof Activity) {
            mNavigator.startDepositForResultActivity((Activity) context);
        }
    }

    @Override
    public void startLinkCardActivity() {
        Object context = getContext();
        Timber.d("startLinkCardActivity context [%s] navigator[%s]", context, mNavigator);
        if (context == null || mNavigator == null) {
            return;
        }
        if (context instanceof Fragment) {
            mNavigator.startLinkCardActivityForResult((Fragment) context);
        } else if (context instanceof Activity) {
            mNavigator.startLinkCardActivityForResult((Activity) context);
        }
    }

    @Override
    public void startLinkAccountActivity() {
        Object context = getContext();
        if (context == null || mNavigator == null) {
            return;
        }
        if (context instanceof Fragment) {
            mNavigator.startLinkAccountActivityForResult((Fragment) context);
        } else if (context instanceof Activity) {
            mNavigator.startLinkAccountActivityForResult((Activity) context);
        }
    }

    @Override
    public void startUpdateProfileBeforeLinkAcc() {
        Object context = getContext();
        if (context == null || mNavigator == null) {
            return;
        }
        if (context instanceof Fragment) {
            mNavigator.startUpdateProfileLevelBeforeLinkAcc((Fragment) context);
        } else if (context instanceof Activity) {
            mNavigator.startUpdateProfileLevelBeforeLinkAcc((Activity) context);
        }
    }
}
