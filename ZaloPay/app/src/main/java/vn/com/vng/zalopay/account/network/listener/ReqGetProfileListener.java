package vn.com.vng.zalopay.account.network.listener;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.Constants;
import vn.com.vng.zalopay.account.models.ZaloProfile;

/**
 * Created by longlv on 22/04/2016.
 */
public class ReqGetProfileListener implements ZaloOpenAPICallback {

    public interface IReqGetProfileListener {
        void onGetProfileSuccess(ZaloProfile zaloProfile);
        void onGetProfileFail(String msg);
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
            ZaloProfile zaloProfile = new ZaloProfile(jsonResult);
            if (mListener != null) {
                mListener.onGetProfileSuccess(zaloProfile);
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
