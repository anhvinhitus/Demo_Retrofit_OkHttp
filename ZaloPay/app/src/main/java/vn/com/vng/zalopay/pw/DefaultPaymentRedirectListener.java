package vn.com.vng.zalopay.pw;

import android.app.Activity;
import android.support.v4.app.Fragment;

import vn.com.vng.zalopay.navigation.Navigator;

/**
 * Created by longlv on 2/10/17.
 * *
 */

public abstract class DefaultPaymentRedirectListener implements PaymentWrapper.IRedirectListener {
    private Navigator mNavigator;

    public DefaultPaymentRedirectListener(Navigator navigator) {
        mNavigator = navigator;
    }

    public abstract Object getContext();

    @Override
    public void startUpdateProfileLevel3() {
        Object context = getContext();
        if (context == null || mNavigator == null) {
            return;
        }
        if (context instanceof Fragment) {
            mNavigator.startUpdateProfile3Activity(((Fragment) context).getActivity(), false);
        } else if (context instanceof Activity) {
            mNavigator.startUpdateProfile3Activity((Activity) context, false);
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
    public void startLinkAccountActivity(String bankCode) {
        Object context = getContext();
        if (context == null || mNavigator == null) {
            return;
        }
        if (context instanceof Fragment) {
            mNavigator.startLinkAccountActivityForResult((Fragment) context, bankCode);
        } else if (context instanceof Activity) {
            mNavigator.startLinkAccountActivityForResult((Activity) context, bankCode);
        }
    }
}
