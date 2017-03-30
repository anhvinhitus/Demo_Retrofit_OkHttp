package vn.com.zalopay.wallet.datasource.task;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.SDKReportImpl;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.Log;

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

    public SDKReportTask(int pExInfo, String... pParams) {
        super();
        this.mExInfo = pExInfo;
        if (pParams != null && pParams.length > 0) {
            this.mTranId = pParams.length >= 1 ? pParams[0] : null;
            this.mException = pParams.length >= 2 ? pParams[1] : null;
            this.mBankCode = pParams.length >= 3 ? pParams[2] : null;
        }

    }

    public SDKReportTask(String... pParams) {
        super();
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
    public static void makeReportError(int pCode, String... pParams) {
        BaseTask baseRequest = new SDKReportTask(pCode, pParams);
        baseRequest.makeRequest();
    }

    /***
     * overlap function
     *
     * @param pParams [transid,exception,bankcode]
     */
    public static void makeReportError(String... pParams) {
        BaseTask baseRequest = new SDKReportTask(pParams);
        baseRequest.makeRequest();
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
        shareDataRepository().setTask(this).postData(new SDKReportImpl(), getDataParams());
    }

    @Override
    protected boolean doParams() {
        return DataParameter.prepareSDKReport(getDataParams(), mTranId, mBankCode, mExInfo, mException);
    }
}
