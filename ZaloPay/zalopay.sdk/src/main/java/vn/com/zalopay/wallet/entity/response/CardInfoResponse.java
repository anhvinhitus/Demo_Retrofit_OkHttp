package vn.com.zalopay.wallet.entity.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.zalopay.wallet.entity.bank.MapCard;

public class CardInfoResponse extends BaseResponse {

    @SerializedName("cardinfochecksum")
    public String cardinfochecksum;

    @SerializedName("cardinfos")
    public List<MapCard> cardinfos;
}
