package vn.com.zalopay.wallet.entity.config;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

import vn.com.zalopay.wallet.entity.bank.BankScript;

public class ConfigBundle {
    @SerializedName("stringMap")
    public HashMap<String, String> stringMap;
    @SerializedName("pageList")
    public List<DPage> pageList;
    @SerializedName("pattern")
    public HashMap<String, HashMap<String, String>> pattern;
    @SerializedName("CCIdentifier")
    public List<CardRule> CCIdentifier;
    @SerializedName("BankIdentifier")
    public List<CardRule> BankIdentifier;
    @SerializedName("bankScripts")
    public List<BankScript> bankScripts;
    @SerializedName("otpReceiverPattern")
    public List<OtpRule> otpReceiverPattern;
    @SerializedName("keyboard")
    public List<DKeyBoardConfig> keyboard;
}
