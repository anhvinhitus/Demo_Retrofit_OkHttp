package vn.com.zalopay.wallet.datasource.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class CheckOrderStatusFailSubmitImpl implements IRequest<StatusResponse> {
    @Override
    public Observable<StatusResponse> getRequest(IData pIData, Map<String, String> pParams) {
        return pIData.checkOrderStatusFailSubmit(pParams);
    }
}
