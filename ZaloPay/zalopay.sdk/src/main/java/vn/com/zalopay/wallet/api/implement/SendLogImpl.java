package vn.com.zalopay.wallet.api.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.constants.ConstantParams;
import vn.com.zalopay.wallet.entity.response.BaseResponse;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.api.interfaces.IRequest;

public class SendLogImpl implements IRequest<BaseResponse> {
    @Override
    public Observable<BaseResponse> getRequest(ITransService pIData, Map<String, String> pParams) {
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
