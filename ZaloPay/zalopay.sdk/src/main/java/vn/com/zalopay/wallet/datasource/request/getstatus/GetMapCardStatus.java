package vn.com.zalopay.wallet.datasource.request.getstatus;

import java.util.HashMap;

import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.GetMapCardStatusImpl;
import vn.com.zalopay.wallet.listener.IDataSourceListener;

public class GetMapCardStatus implements IGetPaymentStatus {
    @Override
    public void onGetStatus(IDataSourceListener pListener, HashMap<String, String> pParamsRequest, boolean pIsRetry) {
        DataRepository.shareInstance().setDataSourceListener(pListener).getDataReuseRequest(new GetMapCardStatusImpl(), pParamsRequest, pIsRetry);
    }

    @Override
    public void onPrepareParamsGetStatus(HashMap<String, String> pParamsRequest, String pTransactionId) throws Exception {
        DataParameter.prepareGetStatusMapCardParams(pParamsRequest, pTransactionId);
    }
}
