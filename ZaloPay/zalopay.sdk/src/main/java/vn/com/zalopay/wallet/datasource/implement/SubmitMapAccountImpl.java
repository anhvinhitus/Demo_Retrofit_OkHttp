package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Response;
import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class SubmitMapAccountImpl implements IRequest<StatusResponse> {
    @Override
    public Observable<Response<StatusResponse>> getRequest(IData pIData, HashMap<String, String> pParams) {
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

    @Override
    public int getRequestEventId() {
        return ZPEvents.CONNECTOR_V001_TPE_SUBMITMAPACCOUNT;
    }
}
