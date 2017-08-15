package vn.com.zalopay.wallet.api.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.api.interfaces.IRequest;
import vn.com.zalopay.wallet.entity.response.BaseResponse;
import vn.com.zalopay.wallet.constants.ConstantParams;

public class SDKReportImpl implements IRequest<BaseResponse> {
    @Override
    public Observable<BaseResponse> getRequest(ITransService pIData, Map<String, String> pParams) {
        return pIData.sdkReport(pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.TRANSID),
                pParams.get(ConstantParams.BANK_CODE),
                pParams.get(ConstantParams.EXINFO),
                pParams.get(ConstantParams.EXCEPTION));
    }
}
