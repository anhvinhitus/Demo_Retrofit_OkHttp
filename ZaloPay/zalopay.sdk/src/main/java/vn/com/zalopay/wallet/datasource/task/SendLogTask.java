package vn.com.zalopay.wallet.datasource.task;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.SendLogImpl;
import vn.com.zalopay.wallet.utils.Log;

public class SendLogTask extends BaseTask<BaseResponse> {
    private String mTransID;
    private int mPmcID;
    private long mCaptchaBeginTime, mCaptchaEndTime;
    private long mOtpBeginTime, mOtpEndTime;

    public SendLogTask(int pPmcID, String pTransID, long pCaptchaBeginTime, long pCaptchaEndTime, long pOtpBeginTime, long pOtpEndTime) {
        super();

        mTransID = pTransID;
        mPmcID = pPmcID;
        mCaptchaBeginTime = pCaptchaBeginTime;
        mCaptchaEndTime = pCaptchaEndTime;
        mOtpBeginTime = pOtpBeginTime;
        mOtpEndTime = pOtpEndTime;
    }

    @Override
    public void onDoTaskOnResponse(BaseResponse pResponse) {

    }

    @Override
    public void onRequestSuccess(BaseResponse pResponse) {
        Log.d(this, "onRequestSuccess");
    }

    @Override
    public void onRequestFail(Throwable e) {
        Log.d(this, e);
    }

    @Override
    public void onRequestInProcess() {
        Log.d(this, "onRequestInProcess");
    }

    @Override
    public String getDefaulErrorNetwork() {
        return null;
    }

    @Override
    protected void doRequest() {
        newDataRepository().setTask(this).postData(new SendLogImpl(), getDataParams());
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareSendLog(mDataParams, mPmcID, mTransID, mCaptchaBeginTime, mCaptchaEndTime, mOtpBeginTime, mOtpEndTime);
            return true;
        } catch (Exception e) {
            onRequestFail(e);
            Log.e(this, e);
            return false;
        }
    }
}
