package vn.com.zalopay.wallet.api.task.getstatus;

import java.util.Map;

import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.ServiceManager;
import vn.com.zalopay.wallet.api.implement.GetTransactionStatusImpl;
import vn.com.zalopay.wallet.api.task.BaseTask;

public class GetPaymentStatus implements IGetPaymentStatus {
    @Override
    public void onGetStatus(BaseTask pTask, Map<String, String> pParamsRequest) {
        ServiceManager.shareInstance().setTask(pTask).loadData(new GetTransactionStatusImpl(), pParamsRequest);
    }

    @Override
    public void onPrepareParamsGetStatus(String pAppId, Map<String, String> pParamsRequest, UserInfo pUserInfo, String pTransactionId) throws Exception {
        DataParameter.prepareGetStatusParams(pAppId, pUserInfo, pParamsRequest, pTransactionId);
    }
}
