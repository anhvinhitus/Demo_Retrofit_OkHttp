package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.CheckOrderStatusFailSubmitImpl;
import vn.com.zalopay.wallet.utils.Log;

/***
 * get status transaction in case error networking
 * for checking whether user submited to server
 */
public class CheckOrderStatusFailSubmit extends BaseRequest<StatusResponse> {
    private String mAppTransID;
    private AdapterBase mAdapter;

    private boolean mRetry;

    /***
     * contructor
     *
     * @param pAdapter
     * @param pAppTransID
     */
    public CheckOrderStatusFailSubmit(AdapterBase pAdapter, String pAppTransID, boolean pRetry) {
        super();
        this.mAppTransID = pAppTransID;
        this.mAdapter = pAdapter;
        this.mRetry = pRetry;
    }

    private void onPostResult() {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_CHECK_STATUS_SUBMIT_COMPLETE, getResponse());
        }

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
            getResponse().returncode = -1;
            getResponse().isprocessing = false;
            getResponse().returnmessage = pMessage;
        }

        onPostResult();
    }

    @Override
    protected void createReponse(int pCode, String pMessage) {
        mResponse = new StatusResponse(pCode, pMessage);
    }

    @Override
    protected void onRequestInProcess() {

    }

    @Override
    protected void doRequest() {
        Log.d(this, "===Begin check status transID: " + mAppTransID);
        try {
            shareDataRepository().getDataReuseRequest(new CheckOrderStatusFailSubmitImpl(), getDataParams(), mRetry);
        } catch (Exception ex) {
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareCheckSubmitOrderStatusParams(getDataParams(), mAppTransID);
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }
        return true;
    }
}
