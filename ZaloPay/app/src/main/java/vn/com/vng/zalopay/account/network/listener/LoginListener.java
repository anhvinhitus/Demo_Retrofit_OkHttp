package vn.com.vng.zalopay.account.network.listener;

import android.app.Dialog;

import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener;
import com.zing.zalo.zalosdk.oauth.OauthResponse;
import com.zing.zalo.zalosdk.oauth.ValidateOAuthCodeCallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import timber.log.Timber;

/**
 * Created by longlv on 21/04/2016.
 * *
 */
public class LoginListener extends OAuthCompleteListener {

    public interface ILoginZaloListener {
        void onAuthError(int errorCode, String message);

        void onGetOAuthComplete(long uId, String oauthCode, String channel);
    }

    private ILoginZaloListener mListener;

    public LoginListener(ILoginZaloListener listener) {
        mListener = listener;
    }

    @Override
    public void onSkipProtectAcc(Dialog dialog) {
        Timber.d("onSkipProtectAcc: onSkipProtectAcc");
        dialog.dismiss();
    }

    @Override
    public void onProtectAccComplete(int errorCode, String message, Dialog dialog) {

        Timber.d("onProtectAccComplete");
        if (errorCode == 0) {

            ZaloSDK.Instance.isAuthenticate(new ValidateOAuthCodeCallback() {

                @Override
                public void onValidateComplete(boolean validated, int error_Code, long userId, String oauthCode) {

                }
            });

            dialog.dismiss();
        }
        if (mListener != null) {
            mListener.onAuthError(-1, message);
        }
    }

    @Override
    public void onAuthenError(int errorCode, String message) {
        Timber.d("onAuthError errorCode: %s message: %s", errorCode, message);

        if (mListener != null) {
            mListener.onAuthError(errorCode, message);
        }
    }

    @Override
    public void onGetOAuthComplete(OauthResponse response) {
        ZaloSDK.Instance.submitAppUserData(String.valueOf(ZaloSDK.Instance.getZaloId()), ZaloSDK.Instance.getLastestLoginChannel(), "zalo", "appUTMSource", null);

        long userId = response.getuId();
        String channel = String.valueOf(response.getChannel());
        String oauthCode = String.valueOf(response.getOauthCode());

        Timber.d("onGetOAuthComplete userId %s oauthCode %s channel %s", userId, oauthCode, channel);

        if (mListener != null) {
            mListener.onGetOAuthComplete(userId, oauthCode, channel);
        }
    }

    @Override
    protected void onRequestAccountProtect(int errorCode, String errorMsg) {
        Timber.d("onRequestAccountProtect");
    }

    @Override
    public void onFinishLoading() {
        Timber.d("onFinishLoading");
    }
}