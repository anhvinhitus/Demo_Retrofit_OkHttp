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

    public ZPTransaction(JSONObject jsonObject) {
        appId = jsonObject.optInt(Constants.APPID);
        transactionToken = jsonObject.optString(Constants.ZPTRANSTOKEN);
    }

    public boolean isValid() {
        return appId > 0 && !TextUtils.isEmpty(transactionToken);
    }
}
