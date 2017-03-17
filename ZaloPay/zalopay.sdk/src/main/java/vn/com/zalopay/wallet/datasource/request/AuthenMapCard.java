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
import vn.com.zalopay.wallet.datasource.implement.AuthenMapCardImpl;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

public class AuthenMapCard extends BaseRequest<StatusResponse> {
    private AdapterBase mAdapter;
    private String mTransID, mAuthenType, mAuthenValue;

    public AuthenMapCard(AdapterBase pAdapter, String pTransID, String pAuthenType, String pAuthenValue) {
        super();
        mAdapter = pAdapter;
        mTransID = pTransID;
        mAuthenType = pAuthenType;
        mAuthenValue = pAuthenValue;
    }

    private void onPostResult() {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_ATM_AUTHEN_PAYER_COMPLETE, getResponse());
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
        if (mAdapter != null) {
            mAdapter.showProgressBar(true, GlobalData.getStringResource(RS.string.zpw_string_authen_atm));
        } else {
            DialogManager.showProcessDialog(BasePaymentActivity.getCurrentActivity(), null);
        }
    }

    @Override
    protected void doRequest() {
        try {
            if (mAdapter.checkNetworkingAndShowRequest()) {
                shareDataRepository().pushData(new AuthenMapCardImpl(), getDataParams());
            }
        } catch (Exception ex) {
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareAtmAuthenPayer(getDataParams(), mTransID, mAuthenType, mAuthenValue);
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }

        return true;
    }
}
