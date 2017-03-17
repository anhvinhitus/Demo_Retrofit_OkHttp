package vn.com.vng.zalopay.ui.subscribe;

import android.app.Activity;
import android.support.annotation.StringRes;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.webview.entity.WebViewPayInfo;

/**
 * Created by longlv on 12/14/16.
 * *
 */

public class MerchantUserInfoSubscribe extends DefaultSubscriber<MerchantUserInfo> {
    private Navigator mNavigator;
    private WeakReference<Activity> mActivity;
    private long mAppId;
    private String mWebViewUrl;

    public MerchantUserInfoSubscribe(Navigator navigator, Activity activity, long appId, String webViewUrl) {
        this.mNavigator = navigator;
        this.mActivity = new WeakReference<>(activity);
        this.mAppId = appId;
        this.mWebViewUrl = webViewUrl;
    }

    @Override
    public void onNext(MerchantUserInfo merchantUserInfo) {
        Timber.d("Get MerchantUserInfo success, merchantInfo [%s]", merchantUserInfo);
        if (mActivity == null || mActivity.get() == null) {
            return;
        }
        if (merchantUserInfo == null) {
            showErrorDialog(R.string.merchant_user_info_invalid);
            return;
        }
        WebViewPayInfo gamePayInfo = new WebViewPayInfo();
        gamePayInfo.setUid(merchantUserInfo.muid);
        gamePayInfo.setAccessToken(merchantUserInfo.maccesstoken);
        gamePayInfo.setAppId(mAppId);
        mNavigator.startServiceWebViewActivity(mActivity.get(), gamePayInfo, mWebViewUrl);
    }

    @Override
    public void onError(Throwable e) {
        Timber.d(e, "Get MerchantUserInfo throw exception");
        if (ResponseHelper.shouldIgnoreError(e)) {
            return;
        }
        if (mActivity == null || mActivity.get() == null) {
            return;
        }
        showErrorDialog(ErrorMessageFactory.create(mActivity.get(), e));
    }

    private void showErrorDialog(@StringRes int strResource) {
        if (mActivity != null && mActivity.get() != null) {
            showErrorDialog(mActivity.get().getString(strResource));
        }
    }

    private void showErrorDialog(String message) {
        if (mActivity != null && mActivity.get() != null) {
            DialogHelper.showNotificationDialog(mActivity.get(), message);
        }
    }
}