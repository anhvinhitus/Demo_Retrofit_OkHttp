package vn.com.zalopay.wallet.api.task;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.implement.AuthenPayerImpl;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.event.SdkAuthenPayerEvent;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;

public class AuthenPayerTask extends BaseTask<StatusResponse> {
    private String mTransID, mAuthenType, mAuthenValue;
    private long startTime = 0;

    public AuthenPayerTask(UserInfo pUserInfo, String pTransID, String pAuthenType, String pAuthenValue) {
        super(pUserInfo);
        mTransID = pTransID;
        mAuthenType = pAuthenType;
        mAuthenValue = pAuthenValue;
    }

    @Override
    public void onDoTaskOnResponse(StatusResponse pResponse) {

    }

    @Override
    public void onRequestSuccess(StatusResponse pResponse) {
        ZPAnalyticsTrackerWrapper.trackApiCall(ZPEvents.CONNECTOR_V001_TPE_ATMAUTHENPAYER, startTime, pResponse);
        mEventBus.postSticky(new SdkAuthenPayerEvent(pResponse));
    }

    @Override
    public void onRequestFail(Throwable e) {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.returncode = -1;
        statusResponse.returnmessage = getDefaulErrorNetwork();
        mEventBus.postSticky(new SdkAuthenPayerEvent(statusResponse));
    }

    @Override
    public void onRequestInProcess() {
        Timber.d("onRequestInProcess");
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getAppContext().getResources().getString(R.string.sdk_error_networking_authenpayer_mess);
    }

    @Override
    protected void doRequest() {
        startTime = System.currentTimeMillis();
        shareDataRepository().setTask(this).postData(new AuthenPayerImpl(), getDataParams());
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareAtmAuthenPayer(getDataParams(), mUserInfo.zalopay_userid, mUserInfo.accesstoken, mTransID, mAuthenType, mAuthenValue);
            return true;
        } catch (Exception e) {
            onRequestFail(e);
            Timber.w(e.getMessage());
            return false;
        }
    }
}
