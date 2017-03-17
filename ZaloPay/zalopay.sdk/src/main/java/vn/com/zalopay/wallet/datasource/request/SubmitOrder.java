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
import vn.com.zalopay.wallet.datasource.implement.SubmitOrderImpl;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class SubmitOrder extends BaseRequest<StatusResponse> {
    protected AdapterBase mAdapter;
    protected String mChannelID;

    public SubmitOrder(AdapterBase pAdapter) {
        super();
        mAdapter = pAdapter;
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_SUBMIT_ORDER_COMPLETED, getResponse());
        }
    }

    @Override
    protected void onRequestFail(String pMessage) {
        Log.d(this, "===onNotifyError===" + pMessage);
        if (mAdapter != null) {
            if (!TextUtils.isEmpty(pMessage)) {
                if (getResponse() == null) {
                    createReponse(-1, pMessage);
                }
                getResponse().returncode = -1;
                getResponse().returnmessage = pMessage;
                getResponse().isprocessing = false;
            }
            mAdapter.onEvent(EEventType.ON_SUBMIT_ORDER_COMPLETED, getResponse());
        }
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
        if (mAdapter.checkNetworkingAndShowRequest()) {
            shareDataRepository().pushData(new SubmitOrderImpl(), getDataParams());
        }
    }

    @Override
    protected boolean doParams() {
        try {
            if (!DataParameter.prepareSubmitTransactionParams(mAdapter, mChannelID, getDataParams())) {
                if (mAdapter != null) {
                    mAdapter.sdkReportError(SDKReport.INVALID_PAYMENTINFO, GsonUtils.toJsonString(GlobalData.getPaymentInfo()) + " params:" + getDataParams().toString());
                }
                onRequestFail(GlobalData.getStringResource(RS.string.zpw_error_paymentinfo));

                return false;
            }
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_payment_info));
            return false;
        }
        return true;
    }
}
