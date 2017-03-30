package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Response;
import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class GetMapCardStatusImpl implements IRequest<StatusResponse> {
    @Override
    public Observable<Response<StatusResponse>> getRequest(IData pIData, HashMap<String, String> pParams) {
        return pIData.getMapCardStatus(pParams);
    }

    @Override
    public int getRequestEventId() {
        return ZPEvents.CONNECTOR_V001_TPE_GETSTATUSMAPCARD;
    }
}
