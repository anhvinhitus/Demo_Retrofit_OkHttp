package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.VerifyMapCardImpl;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.Log;

public class VerifyMapCard extends BaseRequest<StatusResponse> {
    protected AdapterBase mAdapter;

    public VerifyMapCard(AdapterBase pAdapter) {
        super();
        this.mAdapter = pAdapter;
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareVerifyMapCardParams(mAdapter, mDataParams);
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }
        return true;
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_VERIFY_MAPCARD_COMPLETE, getResponse());
        }
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
        mAdapter.onEvent(EEventType.ON_VERIFY_MAPCARD_COMPLETE, getResponse());
    }

    @Override
    protected void onRequestInProcess() {
    }

    @Override
    protected void createReponse(int pCode, String pMessage) {
        mResponse = new StatusResponse(pCode, pMessage);
    }

    @Override
    protected void doRequest() {
        // Lost connection
        if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            mAdapter.showProgressBar(false, null);

            mAdapter.processNetworkingOff();
            return;
        }

        DataRepository.shareInstance().setDataSourceListener(getDataSourceListener()).pushData(new VerifyMapCardImpl(), getDataParams());
    }
}
