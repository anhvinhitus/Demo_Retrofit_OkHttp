package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public interface IGetMapCardInfo {
    void onGetCardInfoComplete(BaseResponse pResponse);
}
