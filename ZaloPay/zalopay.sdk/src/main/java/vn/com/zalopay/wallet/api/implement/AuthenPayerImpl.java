package vn.com.zalopay.wallet.api.implement;


import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.api.interfaces.IRequest;

public class AuthenPayerImpl implements IRequest<StatusResponse> {
    @Override
    public Observable<StatusResponse> getRequest(ITransService pIData, Map<String, String> pParams) {
        return pIData.atmAuthen(pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.ZP_TRANSID),
                pParams.get(ConstantParams.AUTHEN_TYPE),
                pParams.get(ConstantParams.AUTHEN_VALUE),
                pParams.get(ConstantParams.APP_VERSION));
    }
}
