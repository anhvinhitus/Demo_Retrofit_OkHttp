package vn.com.zalopay.wallet.api.task;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.implement.VerifyMapCardImpl;
import vn.com.zalopay.wallet.GlobalData;
import vn.com.zalopay.wallet.entity.bank.PaymentCard;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.event.SdkSubmitOrderEvent;

public class VerifyMapCardTask extends BaseTask<StatusResponse> {
    private PaymentCard mCard;

    public VerifyMapCardTask(UserInfo pUserInfo, PaymentCard pCard) {
        super(pUserInfo);
        this.mCard = pCard;
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareVerifyMapCardParams(mUserInfo, mCard, mDataParams);
            return true;
        } catch (Exception e) {
            onRequestFail(e);
            Timber.w(e.getMessage());
            return false;
        }
    }

    @Override
    public void onDoTaskOnResponse(StatusResponse pResponse) {
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
        Timber.d("onRequestInProcess");
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getAppContext().getResources().getString(R.string.sdk_linkcard_error_networking_verifymapcard_mess);
    }

    @Override
    protected void doRequest() {
        shareDataRepository().setTask(this).postData(new VerifyMapCardImpl(), getDataParams());
    }
}
