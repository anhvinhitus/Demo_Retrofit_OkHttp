package vn.com.zalopay.wallet.api.task;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.implement.VerifyMapCardImpl;

public class VerifyMapCardTask extends BaseTask<StatusResponse> {
    protected AdapterBase mAdapter;

    public VerifyMapCardTask(AdapterBase pAdapter) {
        super(pAdapter.getPaymentInfoHelper().getUserInfo());
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
        Timber.d("onDoTaskOnResponse nothing");
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
        Timber.d(e != null ? e.getMessage() : "Exception");
    }

    @Override
    public void onRequestInProcess() {
        Timber.d("onRequestInProcess");
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getAppContext().getResources().getString(R.string.sdk_linkcard_error_networking_verifymapcard_mess);
    }

    @Override
    protected void doRequest() {
        if (mAdapter.openSettingNetworking()) {
            shareDataRepository().setTask(this).postData(new VerifyMapCardImpl(), getDataParams());
        }
    }
}
