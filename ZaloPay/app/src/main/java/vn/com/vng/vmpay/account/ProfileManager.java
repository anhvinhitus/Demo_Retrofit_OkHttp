package vn.com.vng.vmpay.account;

import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.vmpay.account.listener.IProfileListener;
import vn.com.vng.vmpay.account.models.Profile;
import vn.com.vng.vmpay.account.network.listener.ReqGetProfileListener;
import vn.com.vng.vmpay.account.utils.ProfilePreferences;
import vn.com.vng.zalopay.AndroidApplication;

/**
 * Created by longlv on 22/04/2016.
 */
@Singleton
public class ProfileManager {

    private Profile mProfile;

    @Inject
    public ProfileManager() {
        if (mProfile == null) {
            mProfile = ProfilePreferences.getProfile();
        }
    }

    public void getProfile(final IProfileListener profileListener) {
        //Lay profile tu Shared preferences
        if (mProfile != null && mProfile.getUserId() > 0) {
            if (profileListener != null) {
                profileListener.onGetProfileSuccess(mProfile);
            }
            //Check xem lan cuoi lay thong tin user la khi nao & goi REQUEST de UPDATE profile
            ZaloSDK.Instance.getProfile(AndroidApplication.instance(), new ReqGetProfileListener(new ReqGetProfileListener.IReqGetProfileListener() {
                @Override
                public void onGetProfileSuccess(Profile profile) {
                    mProfile = profile;
                }

                @Override
                public void onGetProfileFail(String msg) {

                }
            }));
            return;
        }

        //Lay profile tu server
        ZaloSDK.Instance.getProfile(AndroidApplication.instance(), new ReqGetProfileListener(new ReqGetProfileListener.IReqGetProfileListener() {
            @Override
            public void onGetProfileSuccess(Profile profile) {
                mProfile = profile;
                if (profileListener != null) {
                    profileListener.onGetProfileSuccess(profile);
                }
            }

            @Override
            public void onGetProfileFail(String msg) {
                if (profileListener != null) {
                    profileListener.onGetProfileFail(msg);
                }
            }
        }));
    }
}
