package vn.com.zalopay.wallet.datasource.task;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.VerifyMapCardImpl;
import vn.com.zalopay.wallet.utils.Log;

public class VerifyMapCardTask extends BaseTask<StatusResponse> {
    protected AdapterBase mAdapter;

    public VerifyMapCardTask(AdapterBase pAdapter) {
        super();
        this.mAdapter = pAdapter;
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareVerifyMapCardParams(mAdapter, mDataParams);
            return true;
        } catch (Exception e) {
            onRequestFail(e);
            Log.e(this, e);
            return false;
        }
    }

    @Override
    public void onDoTaskOnResponse(StatusResponse pResponse) {
        Log.d(this, "onDoTaskOnResponse nothing");
    }

    @Override
    public void onRequestSuccess(StatusResponse pResponse) {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_VERIFY_MAPCARD_COMPLETE, pResponse);
        } else {
            Log.e(this, "mAdapter = NULL");
        }
    }

    @Override
    public void onRequestFail(Throwable e) {
        if (mAdapter != null) {
            StatusResponse statusResponse = new StatusResponse();
            statusResponse.isprocessing = false;
            statusResponse.returncode = -1;
            statusResponse.returnmessage = getDefaulErrorNetwork();
            mAdapter.onEvent(EEventType.ON_VERIFY_MAPCARD_COMPLETE, statusResponse);
        }
        Log.d(this, e);
    }

    @Override
    public void onRequestInProcess() {
        Log.d(this, "onRequestInProcess");
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getStringResource(RS.string.zpw_alert_network_error_verifymapcard);
    }

    @Override
    protected void doRequest() {
        if (mAdapter.checkNetworkingAndShowRequest()) {
            shareDataRepository().setTask(this).postData(new VerifyMapCardImpl(), getDataParams());
        }
    }
}
