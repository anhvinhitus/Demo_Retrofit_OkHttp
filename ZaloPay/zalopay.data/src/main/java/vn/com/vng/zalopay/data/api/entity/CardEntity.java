package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 4/28/16.
 */
public class CardEntity {

    @SerializedName("cardhash")
    public String cardhash;

    @SerializedName("cardname")
    public String cardname;

    @SerializedName("first6cardno")
    public String first6cardno;

    @SerializedName("last4cardno")
    public String last4cardno;

    @SerializedName("bankcode")
    public String bankcode;

}
