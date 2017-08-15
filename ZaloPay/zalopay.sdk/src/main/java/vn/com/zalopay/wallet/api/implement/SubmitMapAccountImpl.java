package vn.com.zalopay.wallet.api.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.constants.ConstantParams;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.api.interfaces.IRequest;

public class SubmitMapAccountImpl implements IRequest<StatusResponse> {
    @Override
    public Observable<StatusResponse> getRequest(ITransService pIData, Map<String, String> pParams) {
        return pIData.submitMapAccount(
                pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ZALO_ID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.BANK_ACCOUNT_INFO),
                pParams.get(ConstantParams.PLATFORM),
                pParams.get(ConstantParams.DEVICE_ID),
                pParams.get(ConstantParams.APP_VERSION),
                pParams.get(ConstantParams.MNO),
                pParams.get(ConstantParams.SDK_VERSION),
                pParams.get(ConstantParams.OS_VERSION),
                pParams.get(ConstantParams.DEVICE_MODEL),
                pParams.get(ConstantParams.CONN_TYPE)
        );
    }
}
