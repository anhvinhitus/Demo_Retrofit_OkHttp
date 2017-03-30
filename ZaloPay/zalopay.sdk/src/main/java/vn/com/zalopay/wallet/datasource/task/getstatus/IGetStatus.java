package vn.com.zalopay.wallet.datasource.task.getstatus;

import java.util.HashMap;

import vn.com.zalopay.wallet.datasource.task.BaseTask;

public interface IGetStatus {
    void onGetStatus(BaseTask pTask, HashMap<String, String> pParamsRequest);
}
