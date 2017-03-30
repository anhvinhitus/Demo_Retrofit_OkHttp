package vn.com.zalopay.wallet.datasource.task.getstatus;

import java.util.HashMap;

public interface IPrepareParamsGetStatus {
    void onPrepareParamsGetStatus(HashMap<String, String> pParamsRequest, String pTransactionId) throws Exception;
}
