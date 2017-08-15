package vn.com.zalopay.wallet.entity.config;


import com.google.gson.annotations.SerializedName;

public class OtpRule {
    @SerializedName("sender")
    public String sender;
    @SerializedName("bankcode")
    public String bankcode;
    @SerializedName("start")
    public int start;
    @SerializedName("length")
    public int length;
    @SerializedName("begin")
    public boolean begin = false;//true read sms from the begining of content.
    @SerializedName("isdigit")
    public boolean isdigit = true;//otp is all digital
}
