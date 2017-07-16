package vn.com.zalopay.wallet.api.task;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.implement.SubmitOrderImpl;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

public class SubmitOrderTask extends BaseTask<StatusResponse> {
    protected AdapterBase mAdapter;
    private long startTime = 0, endTime = 0;

    public SubmitOrderTask(AdapterBase pAdapter) {
        super(pAdapter.getPaymentInfoHelper().getUserInfo());
        mAdapter = pAdapter;
    }

    @Override
    public void onDoTaskOnResponse(StatusResponse pResponse) {
        Timber.d("onDoTaskOnResponse nothing");
        //tracking api call app transid
        endTime = System.currentTimeMillis();
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.trackApiTiming(ZPEvents.CONNECTOR_V001_TPE_SUBMITTRANS, startTime, endTime, pResponse);
        }
    }

    @Override
    public void onRequestSuccess(StatusResponse pResponse) {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_SUBMIT_ORDER_COMPLETED, pResponse);
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
            mAdapter.onEvent(EEventType.ON_SUBMIT_ORDER_COMPLETED, statusResponse);
        }
        Timber.d(e != null ? e.getMessage() : "Exception");
    }

    @Override
    public void onRequestInProcess() {
        Timber.d("onRequestInProcess");
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getAppContext().getResources().getString(R.string.sdk_error_generic_submitorder);
    }

    @Override
    protected void doRequest() {
        if (mAdapter.openSettingNetworking()) {
            startTime = System.currentTimeMillis();
            shareDataRepository().setTask(this).postData(new SubmitOrderImpl(), getDataParams());
        }
    }

    @Override
    protected boolean doParams() {
        try {
            int channeId = mAdapter.getChannelID();
            PaymentInfoHelper paymentInfoHelper = mAdapter.getPaymentInfoHelper();
            long appId = paymentInfoHelper.getAppId();
            AbstractOrder order = paymentInfoHelper.getOrder();
            UserInfo userInfo = paymentInfoHelper.getUserInfo();
            PaymentLocation location = paymentInfoHelper.getLocation();
            @TransactionType int transtype = paymentInfoHelper.getTranstype();
            String chargeInfo = paymentInfoHelper.getChargeInfo(mAdapter.getCard());
            String hashPassword = null;
            return DataParameter.prepareSubmitTransactionParams(channeId, appId, chargeInfo, hashPassword,
                    order, userInfo, location, transtype, getDataParams());
        } catch (Exception e) {
            onRequestFail(e);
            Log.e(this, e);
            return false;
        }
    }
}
