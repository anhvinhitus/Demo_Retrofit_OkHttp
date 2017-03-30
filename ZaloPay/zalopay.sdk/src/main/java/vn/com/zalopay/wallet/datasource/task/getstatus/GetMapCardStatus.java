package vn.com.zalopay.wallet.datasource.task.getstatus;

import java.util.HashMap;

import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.GetMapCardStatusImpl;
import vn.com.zalopay.wallet.datasource.task.BaseTask;

public class GetMapCardStatus implements IGetPaymentStatus {
    @Override
    public void onGetStatus(BaseTask pTask, HashMap<String, String> pParamsRequest) {
        DataRepository.shareInstance().setTask(pTask).loadData(new GetMapCardStatusImpl(), pParamsRequest);
    }

    @Override
    public void onPrepareParamsGetStatus(HashMap<String, String> pParamsRequest, String pTransactionId) throws Exception {
        DataParameter.prepareGetStatusMapCardParams(pParamsRequest, pTransactionId);
    }
}
