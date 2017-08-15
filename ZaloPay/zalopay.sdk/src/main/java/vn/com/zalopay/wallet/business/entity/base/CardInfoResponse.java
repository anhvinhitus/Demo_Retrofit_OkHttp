package vn.com.zalopay.wallet.business.entity.base;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;

public class CardInfoResponse extends BaseResponse {

    @SerializedName("cardinfochecksum")
    public String cardinfochecksum;

    @SerializedName("cardinfos")
    public List<MapCard> cardinfos;
}
