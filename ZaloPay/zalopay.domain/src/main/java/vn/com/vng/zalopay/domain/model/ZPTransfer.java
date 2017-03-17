package vn.com.vng.zalopay.domain.model;

import android.text.TextUtils;

import org.json.JSONObject;

import vn.com.vng.zalopay.domain.Constants;

/**
 * Created by khattn on 3/16/17.
 * Model of transfer information received from web
 */

public class ZPTransfer {

    public String zpid;
    public long amount;
    public String message;
    public Integer[] errorCodeList;

    public ZPTransfer(JSONObject jsonObject) {
        zpid = jsonObject.optString(Constants.TransferMoneyWebAPI.ZPID);
        amount = jsonObject.optInt(Constants.TransferMoneyWebAPI.AMOUNT);
        message = jsonObject.optString(Constants.TransferMoneyWebAPI.MESSAGE);
        errorCodeList = new Integer[]{1, 2, 3, 4};
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(zpid) && amount > 0;
    }
}
