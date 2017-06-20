package vn.com.zalopay.wallet.api.task;

import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.implement.CheckOrderStatusFailSubmitImpl;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;

/***
 * get status transaction in case error networking
 * for checking whether user submited to server
 */
public class CheckOrderStatusFailSubmit extends BaseTask<StatusResponse> {
    private String mAppTransID;
    private AdapterBase mAdapter;

    /***
     * contructor
     *
     * @param pAdapter
     * @param pAppTransID
     */
    public CheckOrderStatusFailSubmit(AdapterBase pAdapter, String pAppTransID) {
        super(pAdapter.getPaymentInfoHelper().getUserInfo());
        this.mAppTransID = pAppTransID;
        this.mAdapter = pAdapter;
    }

    @Override
    public void onDoTaskOnResponse(StatusResponse pResponse) {

    }

    @Override
    public void onRequestSuccess(StatusResponse pResponse) {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_CHECK_STATUS_SUBMIT_COMPLETE, pResponse);
        } else {
            Log.e(this, "mAdapter = NULL");
        }
    }

    @Override
    public void onRequestFail(Throwable e) {
        if (mAdapter != null) {
            StatusResponse statusResponse = new StatusResponse();
            statusResponse.returncode = -1;
            statusResponse.returnmessage = getDefaulErrorNetwork();
            mAdapter.onEvent(EEventType.ON_CHECK_STATUS_SUBMIT_COMPLETE, statusResponse);
        } else {
            Log.e(this, "mAdapter = NULL");
        }
        Log.d(this, e);
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
        shareDataRepository().setTask(this).loadData(new CheckOrderStatusFailSubmitImpl(), getDataParams());
    }

    @Override
    protected boolean doParams() {
        try {
            UserInfo userInfo = mAdapter.getPaymentInfoHelper().getUserInfo();
            long appId = mAdapter.getPaymentInfoHelper().getAppId();
            DataParameter.prepareGetStatusByAppStransParams(String.valueOf(appId), userInfo.zalopay_userid, mAppTransID, getDataParams());
            return true;
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(e);
            return false;
        }
    }
}
