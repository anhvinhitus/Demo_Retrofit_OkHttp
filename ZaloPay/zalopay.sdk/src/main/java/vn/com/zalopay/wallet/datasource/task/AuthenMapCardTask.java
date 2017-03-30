package vn.com.zalopay.wallet.datasource.task;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.AuthenMapCardImpl;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

public class AuthenMapCardTask extends BaseTask<StatusResponse> {
    private AdapterBase mAdapter;
    private String mTransID, mAuthenType, mAuthenValue;

    public AuthenMapCardTask(AdapterBase pAdapter, String pTransID, String pAuthenType, String pAuthenValue) {
        super();
        mAdapter = pAdapter;
        mTransID = pTransID;
        mAuthenType = pAuthenType;
        mAuthenValue = pAuthenValue;
    }

    @Override
    public void onDoTaskOnResponse(StatusResponse pResponse) {

    }

    @Override
    public void onRequestSuccess(StatusResponse pResponse) {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_ATM_AUTHEN_PAYER_COMPLETE, pResponse);
        }
    }

    @Override
    public void onRequestFail(Throwable e) {
        if (mAdapter != null) {
            StatusResponse statusResponse = new StatusResponse();
            statusResponse.returncode = -1;
            statusResponse.returnmessage = getDefaulErrorNetwork();
            mAdapter.onEvent(EEventType.ON_ATM_AUTHEN_PAYER_COMPLETE, statusResponse);
        }
    }

    @Override
    public void onRequestInProcess() {
        if (mAdapter != null) {
            mAdapter.showProgressBar(true, GlobalData.getStringResource(RS.string.zpw_string_authen_atm));
        } else {
            DialogManager.showProcessDialog(BasePaymentActivity.getCurrentActivity(), null);
        }
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getStringResource(RS.string.zpw_alert_network_error_authenpayer);
    }

    @Override
    protected void doRequest() {
        if (mAdapter.checkNetworkingAndShowRequest()) {
            shareDataRepository().setTask(this).postData(new AuthenMapCardImpl(), getDataParams());
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareAtmAuthenPayer(getDataParams(), mTransID, mAuthenType, mAuthenValue);
            return true;
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(e);
            return false;
        }
    }
}
