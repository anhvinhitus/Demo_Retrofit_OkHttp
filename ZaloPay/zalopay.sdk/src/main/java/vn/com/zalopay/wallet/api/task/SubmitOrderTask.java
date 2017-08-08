package vn.com.zalopay.wallet.api.task;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.implement.SubmitOrderImpl;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.entity.voucher.VoucherInfo;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.event.SdkSubmitOrderEvent;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;

public class SubmitOrderTask extends BaseTask<StatusResponse> {
    PaymentInfoHelper mPaymentHelper;
    int mChannelId;
    DPaymentCard mCard;
    private long startTime = 0;

    public SubmitOrderTask(int pChannelId, DPaymentCard pCard, PaymentInfoHelper paymentInfoHelper) {
        super(paymentInfoHelper.getUserInfo());
        mPaymentHelper = paymentInfoHelper;
        mChannelId = pChannelId;
        mCard = pCard;
    }

    @Override
    public void onDoTaskOnResponse(StatusResponse pResponse) {
        ZPAnalyticsTrackerWrapper.trackApiCall(ZPEvents.CONNECTOR_V001_TPE_SUBMITTRANS, startTime, pResponse);
    }

    @Override
    public void onRequestSuccess(StatusResponse pResponse) {
        mEventBus.postSticky(new SdkSubmitOrderEvent(pResponse));
    }

    @Override
    public void onRequestFail(Throwable e) {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.isprocessing = false;
        statusResponse.returncode = -1;
        statusResponse.returnmessage = getDefaulErrorNetwork();
        mEventBus.postSticky(new SdkSubmitOrderEvent(statusResponse));
    }

    @Override
    public void onRequestInProcess() {
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getAppContext().getResources().getString(R.string.sdk_error_generic_submitorder);
    }

    @Override
    protected void doRequest() {
        startTime = System.currentTimeMillis();
        shareDataRepository().setTask(this).postData(new SubmitOrderImpl(), getDataParams());
    }

    @Override
    protected boolean doParams() {
        try {

            long appId = mPaymentHelper.getAppId();
            AbstractOrder order = mPaymentHelper.getOrder();
            VoucherInfo voucherInfo = mPaymentHelper.getVoucher();
            UserInfo userInfo = mPaymentHelper.getUserInfo();
            PaymentLocation location = mPaymentHelper.getLocation();
            @TransactionType int transtype = mPaymentHelper.getTranstype();
            String chargeInfo = mPaymentHelper.getChargeInfo(mCard);
            return DataParameter.prepareSubmitTransactionParams(mChannelId, appId, chargeInfo, null,
                    order, userInfo, location, transtype, voucherInfo,getDataParams());
        } catch (Exception e) {
            onRequestFail(e);
            Timber.w(e, "Exception do params submit order");
            return false;
        }
    }
}
