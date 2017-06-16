package vn.com.zalopay.wallet.api.task.getstatus;

import java.util.Map;

import vn.com.zalopay.wallet.api.task.BaseTask;

public interface IGetStatus {
    void onGetStatus(BaseTask pTask, Map<String, String> pParamsRequest);
}
