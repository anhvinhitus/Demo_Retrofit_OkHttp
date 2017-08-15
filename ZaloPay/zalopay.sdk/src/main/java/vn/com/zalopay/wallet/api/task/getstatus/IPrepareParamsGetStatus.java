package vn.com.zalopay.wallet.api.task.getstatus;

import java.util.Map;

import vn.com.zalopay.wallet.entity.UserInfo;

public interface IPrepareParamsGetStatus {
    void onPrepareParamsGetStatus(String pAppId, Map<String, String> pParamsRequest, UserInfo pUserInfo, String pTransactionId) throws Exception;
}
