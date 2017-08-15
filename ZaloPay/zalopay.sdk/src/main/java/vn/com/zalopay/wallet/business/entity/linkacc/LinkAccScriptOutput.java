package vn.com.zalopay.wallet.business.entity.linkacc;

import android.text.TextUtils;

import com.google.gson.JsonArray;

import vn.com.zalopay.wallet.business.entity.staticconfig.page.DynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.StaticViewGroup;

/*
 * Created by SinhTT on 21/11/2016.
 */

public class LinkAccScriptOutput {
    public String otpimg;
    public String otpimgsrc;

    public boolean shouldStop = false;
    public String message = null;
    public String info = null;
    public JsonArray walletList = null;
    public JsonArray accNumList = null;
    public JsonArray phoneNumList = null;
    public JsonArray otpValidTypeList = null;

    public JsonArray walletUnRegList = null;
    public JsonArray phoneNumUnRegList = null;

    public String phoneReveiceOTP;
    public String messageResult = null;
    public String messageOTP = null;
    public String messageTimeout = null;

    public boolean isError() {
        return (shouldStop && !TextUtils.isEmpty(message));
    }
}
