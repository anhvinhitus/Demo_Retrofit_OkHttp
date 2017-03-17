package vn.com.zalopay.wallet.datasource.request.getstatus;

import java.util.HashMap;

import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.GetTransactionStatusImpl;
import vn.com.zalopay.wallet.listener.IDataSourceListener;

public class GetPaymentStatus implements IGetPaymentStatus {
    @Override
    public void onGetStatus(IDataSourceListener pListener, HashMap<String, String> pParamsRequest, boolean pIsRetry) {
        DataRepository.shareInstance(SDKApplication.getRetrofit()).setDataSourceListener(pListener).getDataReuseRequest(new GetTransactionStatusImpl(), pParamsRequest, pIsRetry);
    }

    @Override
    public void onPrepareParamsGetStatus(HashMap<String, String> pParamsRequest, String pTransactionId) throws Exception {
        DataParameter.prepareGetStatusParams(pParamsRequest, pTransactionId);
    }
}
