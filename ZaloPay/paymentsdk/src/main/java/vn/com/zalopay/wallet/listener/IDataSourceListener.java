package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

/**
 * Created by chucvv on 24/05/2016.
 */
public interface IDataSourceListener {
    public void onRequestAPIComplete(boolean isSuccess, String message, BaseResponse response);

    public void onRequestAPIProgress();
}
