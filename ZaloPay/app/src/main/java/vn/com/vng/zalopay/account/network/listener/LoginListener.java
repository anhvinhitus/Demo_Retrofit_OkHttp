package vn.com.vng.zalopay.account.network.listener;

import android.app.Dialog;

import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener;
import com.zing.zalo.zalosdk.oauth.OauthResponse;
import com.zing.zalo.zalosdk.oauth.ValidateOAuthCodeCallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import timber.log.Timber;

/**
 * Created by longlv on 21/04/2016.
 */
public class LoginListener extends OAuthCompleteListener {

    public interface ILoginZaloListener {
        public void onAuthenError(int errorCode, String message);
        public void onGetOAuthComplete(long uId, String oauthCode, String channel);
    }

    private ILoginZaloListener mListener;

    public LoginListener(ILoginZaloListener listener) {
        mListener = listener;
    }

    @Override
    public void onSkipProtectAcc(Dialog dialog) {
        dialog.dismiss();
    }

    @Override
    public void onProtectAccComplete(int errorCode, String message, Dialog dialog) {

        if (errorCode == 0) {

            ZaloSDK.Instance.isAuthenticate(new ValidateOAuthCodeCallback() {

                @Override
                public void onValidateComplete(boolean validated, int error_Code, long userId, String oauthCode) {

                }
            });

            dialog.dismiss();
        }
        if (mListener != null) {
            mListener.onAuthenError(-1, message);
        }
    }

    @Override
    public void onAuthenError(int errorCode, String message) {
        Timber.d("onAuthenError............errorCode: %s", errorCode);
        Timber.d("onAuthenError............message: %s", message);
        if (mListener != null) {
            mListener.onAuthenError(errorCode, message);
        }
        super.onAuthenError(errorCode, message);
    }

    @Override
    public void onGetOAuthComplete(OauthResponse response) {
        super.onGetOAuthComplete(response);
        ZaloSDK.Instance.submitAppUserData("" + ZaloSDK.Instance.getZaloId(), ZaloSDK.Instance.getLastestLoginChannel(), "zalo", "appUTMSource", null);
        long userId = response.getuId();
        String channel = String.valueOf(response.getChannel());
        String oauthCode = String.valueOf(response.getOauthCode());
        //getProfile();
        Timber.d("onGetOAuthComplete............userId:%s", userId);
        Timber.d("onGetOAuthComplete............oauthCode:%s", oauthCode);
        Timber.d("onGetOAuthComplete............channel:%s", channel);
        if (mListener != null) {
            mListener.onGetOAuthComplete(userId, oauthCode, channel);
        }

        mListener = null;
    }
}