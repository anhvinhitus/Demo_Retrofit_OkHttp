package vn.com.vng.vmpay.account.network.listener;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.vmpay.account.Constants;
import vn.com.vng.vmpay.account.models.Profile;
import vn.com.vng.vmpay.account.utils.ProfilePreferences;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;

/**
 * Created by longlv on 22/04/2016.
 */
public class ReqGetProfileListener implements ZaloOpenAPICallback {

    public interface IReqGetProfileListener {
        public void onGetProfileSuccess(Profile profile);
        public void onGetProfileFail(String msg);
    }

    public ReqGetProfileListener(IReqGetProfileListener reqGetProfileListener) {
        mListener = reqGetProfileListener;
    }

    private IReqGetProfileListener mListener;

    @Override
    public void onResult(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        Timber.tag("ReqGetProfileListener").d("onResult, jsonObject:" + jsonObject.toString());
        try {
            JSONObject jsonResult = jsonObject.getJSONObject(Constants.RESULT);
            Profile profile = new Profile(jsonResult);
            ProfilePreferences.setProfile(profile);
            if (mListener != null) {
                mListener.onGetProfileSuccess(profile);
            }
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            if (mListener != null) {
                mListener.onGetProfileFail(AndroidApplication.instance().getString(R.string.profile_json_exception));
            }
        }
    }
}
