package vn.com.zalopay.wallet.api.task;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.implement.SubmitMapAccountImpl;

public class SubmitMapAccountTask extends BaseTask<BaseResponse> {
    protected String mBankAccInfo;
    protected AdapterBase mAdapter;
    public SubmitMapAccountTask(AdapterBase pAdapter, String pBankAccInfo) {
        super(pAdapter.getPaymentInfoHelper().getUserInfo());
        mAdapter = pAdapter;
        mBankAccInfo = pBankAccInfo;
    }

    @Override
    public void onDoTaskOnResponse(BaseResponse pResponse) {
        Log.d(this, "onDoTaskOnResponse do nothing");
    }

    @Override
    public void onRequestSuccess(BaseResponse pResponse) {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_SUBMIT_LINKACC_COMPLETED, pResponse);
        }
    }

    @Override
    public void onRequestFail(Throwable e) {
        if (mAdapter != null) {
            StatusResponse statusResponse = new StatusResponse();
            statusResponse.isprocessing = false;
            statusResponse.returncode = -1;
            statusResponse.returnmessage = getDefaulErrorNetwork();
            mAdapter.onEvent(EEventType.ON_SUBMIT_LINKACC_COMPLETED, statusResponse);
        }
        Log.d(this, e);
    }

    @Override
    public void onRequestInProcess() {
        Log.d(this, "onRequestInProcess");
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getStringResource(RS.string.zpw_alert_network_error_submitbankaccount);
    }

    @Override
    protected void doRequest() {
        shareDataRepository().setTask(this).postData(new SubmitMapAccountImpl(), getDataParams());
    }

    @Override
    protected boolean doParams() {
        try {
            if(mAdapter.getPaymentInfoHelper() == null){
                return false;
            }
            UserInfo userInfo = mAdapter.getPaymentInfoHelper().getUserInfo();
            DataParameter.prepareMapAccountParams(getDataParams(), mBankAccInfo, userInfo);
            return true;
        } catch (Exception e) {
            onRequestFail(e);
            Log.e(this, e);
            return false;
        }
    }
}
