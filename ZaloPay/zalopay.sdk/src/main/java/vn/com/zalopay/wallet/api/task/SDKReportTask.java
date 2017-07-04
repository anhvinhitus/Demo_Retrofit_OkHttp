package vn.com.zalopay.wallet.api.task;

import timber.log.Timber;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.implement.SDKReportImpl;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;


public class SDKReportTask extends BaseTask<BaseResponse> {
    public static final int DEFAULT = 0;
    public static final int INVALID_PAYMENTINFO = 1;
    public static final int ERROR_SSL = 2;
    public static final int ERROR_WEBSITE = 3;
    public static final int INVALID_USERPROFILE = 4;
    public static final int TRANSACTION_FAIL = 5;
    public static final int API_ERROR = 6;
    public static final int TIMEOUT_WEBSITE = 7;
    public static final int GENERAL_EXCEPTION = 8;

    protected String mTranId;
    protected String mBankCode;
    protected String mException;
    protected int mExInfo;

    public SDKReportTask(UserInfo pUserInfo, int pExInfo, String... pParams) {
        super(pUserInfo);
        this.mExInfo = pExInfo;
        this.mUserInfo = pUserInfo;
        if (pParams != null && pParams.length > 0) {
            this.mTranId = pParams.length >= 1 ? pParams[0] : null;
            this.mException = pParams.length >= 2 ? pParams[1] : null;
            this.mBankCode = pParams.length >= 3 ? pParams[2] : null;
        }

    }

    public SDKReportTask(UserInfo pUserInfo, String... pParams) {
        super(pUserInfo);
        this.mExInfo = DEFAULT;
        if (pParams != null && pParams.length > 0) {
            this.mTranId = pParams.length >= 1 ? pParams[0] : null;
            this.mException = pParams.length >= 2 ? pParams[1] : null;
            this.mBankCode = pParams.length >= 3 ? pParams[2] : null;
        }
    }

    /***
     * @param pCode
     * @param pParams [transid,exception,bankcode]
     */
    public static void makeReportError(UserInfo pUserInfo, int pCode, String... pParams) {
        BaseTask baseRequest = new SDKReportTask(pUserInfo, pCode, pParams);
        baseRequest.makeRequest();
    }

    @Override
    public void onDoTaskOnResponse(BaseResponse pResponse) {
    }

    @Override
    public void onRequestSuccess(BaseResponse pResponse) {
        Timber.d("onRequestSuccess");
    }

    @Override
    public void onRequestFail(Throwable e) {
        Timber.d(e != null ? e.getMessage() : "Exception");
    }

    @Override
    public void onRequestInProcess() {

    }

    @Override
    public String getDefaulErrorNetwork() {
        return null;
    }

    @Override
    protected void doRequest() {
        if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            Log.e(this, "can not send log report error because networking is offline");
            return;
        }
        newDataRepository().setTask(this).postData(new SDKReportImpl(), getDataParams());
    }

    @Override
    protected boolean doParams() {
        return DataParameter.prepareSDKReport(getDataParams(), mUserInfo.zalopay_userid, mUserInfo.accesstoken, mTranId, mBankCode, mExInfo, mException);
    }
}
