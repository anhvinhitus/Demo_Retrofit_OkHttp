package vn.com.zalopay.wallet.datasource.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfoResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class LoadAppInfoImpl implements IRequest<DAppInfoResponse> {

    @Override
    public Observable<DAppInfoResponse> getRequest(IData pIData, Map<String, String> pParams) {
        return pIData.loadAppInfo(pParams);
    }
}
