package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Response;
import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.entity.base.SaveCardResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class SDKReportImpl implements IRequest<SaveCardResponse> {
    @Override
    public Observable<SaveCardResponse> getRequest(IData pIData, HashMap<String, String> pParams) {
        return pIData.sdkReport(pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.TRANSID),
                pParams.get(ConstantParams.BANK_CODE),
                pParams.get(ConstantParams.EXINFO),
                pParams.get(ConstantParams.EXCEPTION));
    }
}
