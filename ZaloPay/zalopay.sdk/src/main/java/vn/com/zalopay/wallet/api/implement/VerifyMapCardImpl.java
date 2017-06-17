package vn.com.zalopay.wallet.api.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.constants.ConstantParams;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.api.interfaces.IRequest;

public class VerifyMapCardImpl implements IRequest<StatusResponse> {
    @Override
    public Observable<StatusResponse> getRequest(ITransService pIData, Map<String, String> pParams) {
        return pIData.verfiyCardMap(pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.ZALO_ID),
                pParams.get(ConstantParams.DEVICE_ID),
                pParams.get(ConstantParams.PLATFORM),
                pParams.get(ConstantParams.SDK_VERSION),
                pParams.get(ConstantParams.OS_VERSION),
                pParams.get(ConstantParams.CONN_TYPE),
                pParams.get(ConstantParams.MNO),
                pParams.get(ConstantParams.DEVICE_MODEL),
                pParams.get(ConstantParams.CARDINFO),
                pParams.get(ConstantParams.APP_VERSION));
    }
}
