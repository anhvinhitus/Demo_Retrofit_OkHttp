package vn.com.zalopay.wallet.business.entity.base;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

public class CardInfoListResponse extends BaseResponse {
    public String cardinfochecksum = null;
    public List<DMappedCard> cardinfos = null;
}
