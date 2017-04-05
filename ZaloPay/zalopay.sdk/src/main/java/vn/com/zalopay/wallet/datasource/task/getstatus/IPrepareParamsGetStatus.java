package vn.com.zalopay.wallet.datasource.task.getstatus;

import java.util.Map;

public interface IPrepareParamsGetStatus {
    void onPrepareParamsGetStatus(Map<String, String> pParamsRequest, String pTransactionId) throws Exception;
}
