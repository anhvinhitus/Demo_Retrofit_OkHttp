package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Response;
import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class AuthenMapCardImpl implements IRequest<StatusResponse> {
    @Override
    public Observable<Response<StatusResponse>> getRequest(IData pIData, HashMap<String, String> pParams) {
        return pIData.authenMapCard(pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.ZP_TRANSID),
                pParams.get(ConstantParams.AUTHEN_TYPE),
                pParams.get(ConstantParams.AUTHEN_VALUE),
                pParams.get(ConstantParams.APP_VERSION));
    }

    @Override
    public int getRequestEventId() {
        return ZPEvents.CONNECTOR_V001_TPE_AUTHCARDHOLDERFORMAPPING;
    }
}
