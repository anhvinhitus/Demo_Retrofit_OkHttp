package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.linkacc.DSubmitBankAcc;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.SubmitMapAccountImpl;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

/**
 * Created by cpu11843-local on 1/10/17.
 */

public class SubmitMapAccount extends BaseRequest<BaseResponse> {

    protected String mBankAccInfo;
    protected AdapterBase mAdapter;

    public SubmitMapAccount(AdapterBase pAdapter, String pBankAccInfo) {
        super();
        mAdapter = pAdapter;
        mBankAccInfo = pBankAccInfo;
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        if (mAdapter != null) {
            StatusResponse response = GsonUtils.fromJsonString(getResponse().toJsonString(), StatusResponse.class);
            mAdapter.onEvent(EEventType.ON_SUBMIT_LINKACC_COMPLETED, response);
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
            }
            mAdapter.onEvent(EEventType.ON_SUBMIT_LINKACC_COMPLETED, getResponse());
        }
    }

    @Override
    protected void onRequestInProcess() {
        Log.d("InProcess", "...");
    }

    @Override
    protected void doRequest() {
        try {
            DataRepository.shareInstance().setDataSourceListener(getDataSourceListener()).pushData(new SubmitMapAccountImpl(), getDataParams());
        } catch (Exception ex) {
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareMapAccountParams(getDataParams(), mBankAccInfo);
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }
        return true;
    }
}
