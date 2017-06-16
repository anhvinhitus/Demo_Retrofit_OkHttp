package vn.com.zalopay.wallet.api.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.entity.base.SaveCardResponse;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.api.interfaces.IRequest;

public class SDKReportImpl implements IRequest<SaveCardResponse> {
    @Override
    public Observable<SaveCardResponse> getRequest(ITransService pIData, Map<String, String> pParams) {
        return pIData.sdkReport(pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.TRANSID),
                pParams.get(ConstantParams.BANK_CODE),
                pParams.get(ConstantParams.EXINFO),
                pParams.get(ConstantParams.EXCEPTION));
    }
}
