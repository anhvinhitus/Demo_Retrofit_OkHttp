package vn.com.zalopay.wallet.datasource.task;

import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.SendLogImpl;

public class SendLogTask extends BaseTask<BaseResponse> {
    protected UserInfo mUserInfo;
    private String mTransID;
    private int mPmcID;
    private long mCaptchaBeginTime, mCaptchaEndTime;
    private long mOtpBeginTime, mOtpEndTime;

    public SendLogTask(UserInfo pUserInfo, int pPmcID, String pTransID, long pCaptchaBeginTime, long pCaptchaEndTime, long pOtpBeginTime, long pOtpEndTime) {
        super(pUserInfo);
        mTransID = pTransID;
        mPmcID = pPmcID;
        mCaptchaBeginTime = pCaptchaBeginTime;
        mCaptchaEndTime = pCaptchaEndTime;
        mOtpBeginTime = pOtpBeginTime;
        mOtpEndTime = pOtpEndTime;
        mUserInfo = pUserInfo;
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
            DataParameter.prepareSendLog(mDataParams, mUserInfo.zalopay_userid, mUserInfo.accesstoken, mPmcID, mTransID, mCaptchaBeginTime, mCaptchaEndTime, mOtpBeginTime, mOtpEndTime);
            return true;
        } catch (Exception e) {
            onRequestFail(e);
            Log.e(this, e);
            return false;
        }
    }
}
