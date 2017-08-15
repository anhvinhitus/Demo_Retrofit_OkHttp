package vn.com.zalopay.wallet.entity.linkacc;

import com.google.gson.annotations.SerializedName;

/**
 * Created by SinhTT on 21/11/2016.
 */

public class LinkAccScriptInput {
    @SerializedName("isAjax")
    public boolean isAjax = false;

    @SerializedName("username")
    public String username;

    @SerializedName("password")
    public String password;

    @SerializedName("captchaLogin")
    public String captchaLogin;

    @SerializedName("walletType")
    public String walletType;

    @SerializedName("accNum")
    public String accNum;

    @SerializedName("phoneNum")
    public String phoneNum;

    @SerializedName("otpValidType")
    public String otpValidType;

    @SerializedName("captchaConfirm")
    public String captchaConfirm;

    @SerializedName("otp")
    public String otp;

    @SerializedName("linkerType")
    public int linkerType;

    @SerializedName("walletTypeUnregister")
    public String walletTypeUnregister;

    @SerializedName("phoneNumUnregister")
    public String phoneNumUnregister;

    @SerializedName("passwordUnregister")
    public String passwordUnregister;

}
