package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Response;
import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class SendLogImpl implements IRequest<BaseResponse> {
    @Override
    public Observable<BaseResponse> getRequest(IData pIData, HashMap<String, String> pParams) {
        return pIData.sendLog(
                pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.PMC_ID),
                pParams.get(ConstantParams.TRANS_ID),
                pParams.get(ConstantParams.ATM_CAPTCHA_BEGINDATE),
                pParams.get(ConstantParams.ATM_CAPTCHA_ENDDATE),
                pParams.get(ConstantParams.ATM_OTP_BEGINDATE),
                pParams.get(ConstantParams.ATM_OTP_ENDDATE),
                pParams.get(ConstantParams.APP_VERSION));
    }
}
