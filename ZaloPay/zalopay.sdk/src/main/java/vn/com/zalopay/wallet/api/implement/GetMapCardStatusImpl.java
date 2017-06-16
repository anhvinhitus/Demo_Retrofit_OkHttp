package vn.com.zalopay.wallet.api.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.api.IData;
import vn.com.zalopay.wallet.api.interfaces.IRequest;

public class GetMapCardStatusImpl implements IRequest<StatusResponse> {
    @Override
    public Observable<StatusResponse> getRequest(IData pIData, Map<String, String> pParams) {
        return pIData.getMapCardStatus(pParams);
    }
}
