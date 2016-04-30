//package vn.com.vng.vmpay.account;
//
//import com.zing.zalo.zalosdk.oauth.ZaloSDK;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
//import vn.com.vng.vmpay.account.listener.IProfileListener;
//import vn.com.vng.vmpay.account.models.ZaloProfile;
//import vn.com.vng.zalopay.account.network.listener.ReqGetProfileListener;
//import vn.com.vng.vmpay.account.utils.ZaloProfilePreferences;
//import vn.com.vng.zalopay.AndroidApplication;
//
///**
// * Created by longlv on 22/04/2016.
// */
//@Singleton
//public class ZaloProfileManager {
//
//    private ZaloProfile mZaloProfile;
//
//    @Inject
//    public ZaloProfileManager() {
//        if (mZaloProfile == null) {
//            mZaloProfile = ZaloProfilePreferences.getZaloProfile();
//        }
//    }
//
//    public void getProfile(final IProfileListener profileListener) {
//        //Lay profile tu Shared preferences
//        if (mZaloProfile != null && mZaloProfile.getUserId() > 0) {
//            if (profileListener != null) {
//                profileListener.onGetProfileSuccess(mZaloProfile);
//            }
//            //Check xem lan cuoi lay thong tin user la khi nao & goi REQUEST de UPDATE profile
//            ZaloSDK.Instance.getProfile(AndroidApplication.instance(), new ReqGetProfileListener(new ReqGetProfileListener.IReqGetProfileListener() {
//                @Override
//                public void onGetProfileSuccess(ZaloProfile zaloProfile) {
//                    mZaloProfile = zaloProfile;
//                }
//
//                @Override
//                public void onGetProfileFail(String msg) {
//
//                }
//            }));
//            return;
//        }
//
//        //Lay profile tu server
//        ZaloSDK.Instance.getProfile(AndroidApplication.instance(), new ReqGetProfileListener(new ReqGetProfileListener.IReqGetProfileListener() {
//            @Override
//            public void onGetProfileSuccess(ZaloProfile zaloProfile) {
//                mZaloProfile = zaloProfile;
//                if (profileListener != null) {
//                    profileListener.onGetProfileSuccess(zaloProfile);
//                }
//            }
//
//            @Override
//            public void onGetProfileFail(String msg) {
//                if (profileListener != null) {
//                    profileListener.onGetProfileFail(msg);
//                }
//            }
//        }));
//    }
//}
