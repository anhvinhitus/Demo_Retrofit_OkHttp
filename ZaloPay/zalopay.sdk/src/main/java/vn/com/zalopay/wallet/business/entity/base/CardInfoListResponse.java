package vn.com.zalopay.wallet.business.entity.base;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;

public class CardInfoListResponse extends BaseResponse {
    public String cardinfochecksum = null;
    public List<MapCard> cardinfos = null;
}
