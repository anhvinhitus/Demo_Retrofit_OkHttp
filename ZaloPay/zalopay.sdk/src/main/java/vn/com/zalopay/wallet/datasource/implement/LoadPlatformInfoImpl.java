package vn.com.zalopay.wallet.datasource.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PlatformInfoResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class LoadPlatformInfoImpl implements IRequest<PlatformInfoResponse> {

    @Override
    public Observable<PlatformInfoResponse> getRequest(IData pIData, Map<String, String> pParams) {
        return pIData.loadPlatformInfo(pParams);
    }
}
