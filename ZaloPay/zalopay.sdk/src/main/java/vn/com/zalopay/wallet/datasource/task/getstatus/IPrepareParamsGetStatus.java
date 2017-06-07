package vn.com.zalopay.wallet.datasource.task.getstatus;

import java.util.Map;

import vn.com.zalopay.wallet.business.entity.user.UserInfo;

public interface IPrepareParamsGetStatus {
    void onPrepareParamsGetStatus(String pAppId, Map<String, String> pParamsRequest, UserInfo pUserInfo, String pTransactionId) throws Exception;
}
