package vn.com.zalopay.wallet.entity.bank;

import com.google.gson.annotations.SerializedName;

public class AtmScriptInput {

    @SerializedName("isAjax")
    public boolean isAjax = false;

    @SerializedName("cardHolderName")
    public String cardHolderName;

    @SerializedName("cardNumber")
    public String cardNumber;

    @SerializedName("cardMonth")
    public String cardMonth;

    @SerializedName("cardYear")
    public String cardYear;

    @SerializedName("cardPass")
    public String cardPass;

    @SerializedName("captcha")
    public String captcha;

    @SerializedName("otp")
    public String otp;

    @SerializedName("username")
    public String username;

    @SerializedName("password")
    public String password;
}
