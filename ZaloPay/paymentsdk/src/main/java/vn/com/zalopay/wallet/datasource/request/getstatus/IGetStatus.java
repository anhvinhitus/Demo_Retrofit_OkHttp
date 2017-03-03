package vn.com.zalopay.wallet.datasource.request.getstatus;

import java.util.HashMap;

import vn.com.zalopay.wallet.listener.IDataSourceListener;

public interface IGetStatus {
    void onGetStatus(IDataSourceListener pListener, HashMap<String, String> pParamsRequest, boolean pIsRetry);
}
