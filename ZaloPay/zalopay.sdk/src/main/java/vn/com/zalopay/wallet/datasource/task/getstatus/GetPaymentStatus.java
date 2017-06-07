package vn.com.zalopay.wallet.datasource.task.getstatus;

import java.util.Map;

import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.GetTransactionStatusImpl;
import vn.com.zalopay.wallet.datasource.task.BaseTask;

public class GetPaymentStatus implements IGetPaymentStatus {
    @Override
    public void onGetStatus(BaseTask pTask, Map<String, String> pParamsRequest) {
        DataRepository.shareInstance().setTask(pTask).loadData(new GetTransactionStatusImpl(), pParamsRequest);
    }

    @Override
    public void onPrepareParamsGetStatus(String pAppId, Map<String, String> pParamsRequest, UserInfo pUserInfo, String pTransactionId) throws Exception {
        DataParameter.prepareGetStatusParams(pAppId, pUserInfo, pParamsRequest, pTransactionId);
    }
}
