package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

public interface ZPWRemoveMapCardListener {
    void onSuccess(DMappedCard mapCard);
    void onError(BaseResponse pMessage);
}
