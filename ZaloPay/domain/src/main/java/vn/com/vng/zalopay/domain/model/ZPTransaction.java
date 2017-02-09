package vn.com.vng.zalopay.domain.model;

import android.text.TextUtils;

import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.domain.Constants;

/**
 * Created by longlv on 2/9/17.
 * *
 */

public class ZPTransaction {

    public long appId;
    public String transactionToken;

    public ZPTransaction(JSONObject jsonObject) throws IllegalArgumentException {
        long appId = jsonObject.optInt(Constants.APPID);
        Timber.d("AppID: %d", appId);
        String transactionToken = jsonObject.optString(Constants.ZPTRANSTOKEN);
        Timber.d("Transtoken: %s", transactionToken);
    }

    public boolean isValid() {
        return !(appId < 0 || TextUtils.isEmpty(transactionToken));
    }
}
