package vn.com.zalopay.wallet.datasource.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class LoadPlatformInfoImpl implements IRequest<DPlatformInfo> {

    @Override
    public Observable<DPlatformInfo> getRequest(IData pIData, Map<String, String> pParams) {
        return pIData.loadPlatformInfo(pParams);
    }
}
