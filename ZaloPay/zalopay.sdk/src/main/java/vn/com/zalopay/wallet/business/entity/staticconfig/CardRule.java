package vn.com.zalopay.wallet.business.entity.staticconfig;

import com.google.gson.annotations.SerializedName;

public class CardRule {
    @SerializedName("code")
    public String code;
    @SerializedName("name")
    public String name;
    @SerializedName("startPin")
    public String startPin;
    @SerializedName("min_length")
    public int min_length;
    @SerializedName("max_length")
    public int max_length;

    public boolean isMatchMaxLengthCard(int pLength) {
        return max_length > 0 && pLength == max_length;

    }
}
