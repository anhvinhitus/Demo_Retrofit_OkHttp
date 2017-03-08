package vn.com.zalopay.wallet.business.entity.linkacc;

import android.text.TextUtils;

import com.google.gson.JsonArray;

import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;

/**
 * Created by SinhTT on 21/11/2016.
 */

public class DLinkAccScriptOutput {
    public int pageId = 0;

    public String otpimg;
    public String otpimgsrc;

    public boolean shouldStop = false;
    public String message = null;
    public String info = null;

    public String accountList = null;

    public DStaticViewGroup staticView = null;
    public DDynamicViewGroup dynamicView = null;

    public JsonArray walletList = null;
    public JsonArray accNumList = null;
    public JsonArray phoneNumList = null;
    public JsonArray otpValidTypeList = null;

    public JsonArray walletUnRegList = null;
    public JsonArray phoneNumUnRegList = null;

    public String phoneReveiceOTP;
    public String note = null;
    public String messageResult = null;
    public String messageOTP = null;
    public String messageTimeout = null;

    public boolean isError() {
        return (shouldStop == true && !TextUtils.isEmpty(message));
    }
}
