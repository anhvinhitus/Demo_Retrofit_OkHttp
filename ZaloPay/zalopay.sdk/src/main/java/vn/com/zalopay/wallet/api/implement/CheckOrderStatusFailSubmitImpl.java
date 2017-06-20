package vn.com.zalopay.wallet.api.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.api.interfaces.IRequest;

public class CheckOrderStatusFailSubmitImpl implements IRequest<StatusResponse> {
    @Override
    public Observable<StatusResponse> getRequest(ITransService pIData, Map<String, String> pParams) {
        return pIData.getStatusByAppTransClient(pParams);
    }
}
