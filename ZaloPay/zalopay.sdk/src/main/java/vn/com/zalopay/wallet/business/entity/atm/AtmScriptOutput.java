package vn.com.zalopay.wallet.business.entity.atm;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import vn.com.zalopay.wallet.business.entity.staticconfig.page.DynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.StaticViewGroup;

public class AtmScriptOutput {
    @SerializedName("eventID")
    public int eventID = 0;
    @SerializedName("otpimg")
    public String otpimg;
    @SerializedName("otpimgsrc")
    public String otpimgsrc;
    @SerializedName("shouldStop")
    public boolean shouldStop = false;
    @SerializedName("message")
    public String message;
    @SerializedName("info")
    public String info;
    @SerializedName("staticView")
    public StaticViewGroup staticView;
    @SerializedName("dynamicView")
    public DynamicViewGroup dynamicView;

    public boolean isError() {
        return (shouldStop && !TextUtils.isEmpty(message));
    }
}
