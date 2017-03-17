package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.SDKReportImpl;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class SDKReport extends BaseRequest<BaseResponse> {
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

    public SDKReport(int pExInfo, String... pParams) {
        super();
        this.mExInfo = pExInfo;
        if (pParams != null && pParams.length > 0) {
            this.mTranId = pParams.length >= 1 ? pParams[0] : null;
            this.mException = pParams.length >= 2 ? pParams[1] : null;
            this.mBankCode = pParams.length >= 3 ? pParams[2] : null;
        }

    }

    public SDKReport(String... pParams) {
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
        BaseRequest baseRequest = new SDKReport(pCode, pParams);
        baseRequest.makeRequest();
    }

    /***
     * overlap function
     *
     * @param pParams [transid,exception,bankcode]
     */
    public static void makeReportError(String... pParams) {
        BaseRequest baseRequest = new SDKReport(pParams);
        baseRequest.makeRequest();
    }

    private void onPostResult() {
        Log.d(this, GsonUtils.toJsonString(getResponse()));
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        onPostResult();
    }

    @Override
    protected void onRequestFail(String pMessage) {

        if (!TextUtils.isEmpty(pMessage)) {
            if (getResponse() == null) {
                createReponse(-1, pMessage);
            }

            mResponse.returncode = -1;
            mResponse.returnmessage = pMessage;
        }

        onPostResult();
    }

    @Override
    protected void createReponse(int pCode, String pMessage) {
        mResponse = new BaseResponse();
    }

    @Override
    protected void onRequestInProcess() {

    }

    @Override
    protected void doRequest() {
        try {
            if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
                Log.e("makeReportError", "====cant send log report error because networking is offline");
                return;
            }
           shareDataRepository().pushData(new SDKReportImpl(), getDataParams());
        } catch (Exception ex) {
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        return DataParameter.prepareSDKReport(getDataParams(), mTranId, mBankCode, mExInfo, mException);
    }
}
