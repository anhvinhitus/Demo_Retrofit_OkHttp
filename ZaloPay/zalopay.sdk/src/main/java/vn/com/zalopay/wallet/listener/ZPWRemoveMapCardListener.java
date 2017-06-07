package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;

public interface ZPWRemoveMapCardListener {
    void onSuccess(MapCard mapCard);
    void onError(BaseResponse pMessage);
}
