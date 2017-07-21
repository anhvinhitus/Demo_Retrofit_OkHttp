package vn.com.zalopay.wallet.api.task;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.implement.CheckOrderStatusFailSubmitImpl;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.event.SdkCheckSubmitOrderEvent;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;

/***
 * get status transaction in case error networking
 * for checking whether user submited to server
 */
public class CheckOrderStatusFailSubmit extends BaseTask<StatusResponse> {
    private String mAppTransID;
    private long mAppId;
    private long startTime = 0;

    public CheckOrderStatusFailSubmit(String pAppTransID, long appId, UserInfo userInfo) {
        super(userInfo);
        this.mAppTransID = pAppTransID;
        this.mAppId = appId;
    }

    @Override
    public void onDoTaskOnResponse(StatusResponse pResponse) {
    }

    @Override
    public void onRequestSuccess(StatusResponse pResponse) {
        ZPAnalyticsTrackerWrapper.trackApiCall(ZPEvents.CONNECTOR_V001_TPE_GETSTATUSBYAPPTRANSIDFORCLIENT, startTime, pResponse);
        mEventBus.postSticky(new SdkCheckSubmitOrderEvent(pResponse));
    }

    @Override
    public void onRequestFail(Throwable e) {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.returncode = -1;
        statusResponse.returnmessage = getDefaulErrorNetwork();
        mEventBus.postSticky(new SdkCheckSubmitOrderEvent(statusResponse));
    }

    @Override
    public void onRequestInProcess() {

    }

    @Override
    public String getDefaulErrorNetwork() {
        return null;
    }

    @Override
    protected void doRequest() {
        startTime = System.currentTimeMillis();
        shareDataRepository().setTask(this).loadData(new CheckOrderStatusFailSubmitImpl(), getDataParams());
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareGetStatusByAppStransParams(String.valueOf(mAppId), mUserInfo.zalopay_userid, mAppTransID, getDataParams());
            return true;
        } catch (Exception e) {
            Timber.w(e, "Exception do param check order submit");
            onRequestFail(e);
            return false;
        }
    }
}
