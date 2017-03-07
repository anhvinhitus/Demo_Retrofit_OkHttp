package vn.com.zalopay.wallet.datasource.request;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.SendLogImpl;
import vn.com.zalopay.wallet.utils.Log;

public class SendLog extends BaseRequest<BaseResponse> {
    private String mTransID;

    private int mPmcID;

    private long mCaptchaBeginTime, mCaptchaEndTime;

    private long mOtpBeginTime, mOtpEndTime;

    public SendLog(int pPmcID, String pTransID, long pCaptchaBeginTime, long pCaptchaEndTime, long pOtpBeginTime, long pOtpEndTime) {
        super();

        mTransID = pTransID;
        mPmcID = pPmcID;
        mCaptchaBeginTime = pCaptchaBeginTime;
        mCaptchaEndTime = pCaptchaEndTime;
        mOtpBeginTime = pOtpBeginTime;
        mOtpEndTime = pOtpEndTime;
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        Log.d(this, getResponse() != null ? getResponse().toString() : "networking - error");
    }

    @Override
    protected void onRequestFail(String pMessage) {
        Log.e(this, "===onRequestFail===" + pMessage);
    }

    @Override
    protected void onRequestInProcess() {

    }

    @Override
    protected void doRequest() {
        try {
            DataRepository.newInstance().setDataSourceListener(getDataSourceListener()).pushDataNoCheckDuplicate(new SendLogImpl(), getDataParams());
        } catch (Exception ex) {
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareSendLog(mDataParams, mPmcID, mTransID, mCaptchaBeginTime, mCaptchaEndTime, mOtpBeginTime, mOtpEndTime);
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }
        return true;
    }
}
