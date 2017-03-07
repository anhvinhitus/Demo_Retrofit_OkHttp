package vn.com.zalopay.wallet.datasource.request.getstatus;

import java.util.HashMap;

public interface IPrepareParamsGetStatus {
    void onPrepareParamsGetStatus(HashMap<String, String> pParamsRequest, String pTransactionId) throws Exception;
}
