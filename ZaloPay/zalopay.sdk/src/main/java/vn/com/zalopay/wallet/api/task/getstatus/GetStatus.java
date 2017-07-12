package vn.com.zalopay.wallet.api.task.getstatus;

import android.os.CountDownTimer;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.wallet.api.task.BaseTask;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;

/***
 * get transaction status class
 */
public class GetStatus extends BaseTask<StatusResponse> {
    protected static CountDownTimer mTimer;//coundown timer to retry get status
    protected long mAppId;
    @TransactionType
    int transtype;
    private String mTransID;
    private boolean mIsNeedToCheckDataInResponse;
    private AdapterBase mAdapter;
    private boolean isTimerStated = false;
    private String mMessage;
    private int mRetryCount = 1;
    private long startTime = 0, endTime = 0;
    public GetStatus(AdapterBase pAdapter, String pTransID, boolean pIsCheckData, String pMessage) {
        super(pAdapter.getPaymentInfoHelper().getUserInfo());
        this.mTransID = pTransID;
        this.mIsNeedToCheckDataInResponse = pIsCheckData;
        this.mAdapter = pAdapter;
        this.mMessage = pMessage;
        this.transtype = mAdapter.getPaymentInfoHelper().getTranstype();
        initTimer();
    }

    public static void cancelRetryTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private void initTimer() {
        //reduce double to zalopay channel
        int intervalRetry = Constants.SLEEPING_INTERVAL_OF_RETRY;
        if (GlobalData.isZalopayChannel(mAppId))
            intervalRetry /= 2;

        mTimer = new CountDownTimer(Constants.MAX_INTERVAL_OF_RETRY, intervalRetry) {
            public void onTick(long millisUntilFinished) {
                makeRequest();
            }

            public void onFinish() {
                cancelTimer();
                askToRetryGetStatus(GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_ask_to_retry));
            }
        };
    }

    private void startTimer() {
        if (mTimer != null && !isTimerStated) {
            isTimerStated = true;
            mTimer.start();
        }

    }

    private void cancelTimer() {
        if (mTimer != null && isTimerStated) {
            isTimerStated = false;
            mTimer.cancel();
        }
    }

    private synchronized void askToRetryGetStatus(String pMessage) {
        showProgress(false);
        if (mAdapter != null && mAdapter.isFinalScreen()) {
            Timber.d("user in fail screen, don't need show retry dialog again");
            return;
        }

        if (mRetryCount == Constants.MAX_RETRY_GETSTATUS) {
            onPostResult(createReponse(-1,  GlobalData.getAppContext().getString(GlobalData.getTransProcessingMessage(transtype))));
            return;
        }

        try {
            mAdapter.getView().showRetryDialog(pMessage, new ZPWOnEventConfirmDialogListener() {
                @Override
                public void onCancelEvent() {
                    onPostResult(createReponse(-1, GlobalData.getAppContext().getString(GlobalData.getTransProcessingMessage(transtype))));
                }

                @Override
                public void onOKEvent() {
                    mRetryCount++;
                    makeRequest();
                }
            });
        } catch (Exception e) {
            Log.e(this, e);
            onPostResult(createReponse(-1,  GlobalData.getAppContext().getString(GlobalData.getTransProcessingMessage(transtype))));
        }
    }

    private void onPostResult(StatusResponse pResponse) {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_GET_STATUS_COMPLETE, pResponse);
        } else {
            Log.e(this, "mAdapter = NULL");
        }
    }

    private void showProgress(boolean pIsShow) {
        if (TextUtils.isEmpty(mMessage)) {
            mMessage = GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing);
        }
        try {
            if (pIsShow) {
                mAdapter.getView().showLoading(GlobalData.getStringResource(RS.string.zpw_string_authen_atm));
            } else {
                mAdapter.getView().hideLoading();
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected StatusResponse createReponse(int pCode, String pMessage) {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.isprocessing = false;
        statusResponse.returncode = pCode;
        statusResponse.returnmessage = !TextUtils.isEmpty(pMessage) ? pMessage : getDefaulErrorNetwork();
        return statusResponse;
    }

    @Override
    public void onDoTaskOnResponse(StatusResponse pResponse) {

    }

    @Override
    public void onRequestSuccess(StatusResponse pResponse) {
        endTime = System.currentTimeMillis();
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.trackApiTiming(ZPEvents.CONNECTOR_V001_TPE_GETTRANSSTATUS, startTime, endTime, pResponse);
        }
        if (pResponse == null) {
            cancelTimer();
            askToRetryGetStatus(GlobalData.getStringResource(RS.string.zingpaysdk_alert_error_networking_ask_to_retry));
            return;
        }
        //getResponse().data = "{\"actiontype\":1,\"redirecturl\":\"ac2pl\"}";
        //pResponse.isprocessing = true;
        //((StatusResponse) getResponse()).returncode = -1;
        //stop get status right away if wrong otp
//        ((StatusResponse) getResponse()).returncode = Constants.PAYMENT_LIMIT_PER_DAY_CODE.get(0);
        if (PaymentStatusHelper.isWrongOtpResponse(pResponse)) {
            cancelTimer();
            onPostResult(pResponse);
            return;
        }
        //flow retry oneshot if load bank's website timeout
        if (mAdapter.isLoadWebTimeout() && pResponse.isprocessing) {
            pResponse.isprocessing = false;
            pResponse.returncode = -1;
            pResponse.returnmessage =  GlobalData.getAppContext().getString(GlobalData.getTransProcessingMessage(transtype));

            mAdapter.setLoadWebTimeout(false);
            cancelTimer();
            onPostResult(pResponse);
            Timber.d("load website timeout");
            return;
        }
        if(!mIsNeedToCheckDataInResponse){
            pResponse.data = null;
        }
        //flow 3ds
        if (TransactionHelper.isSecurityFlow(pResponse)) {
            cancelTimer();
            onPostResult(pResponse);
        }
        //continue get status  because order still is processing
        else if (pResponse.isprocessing) {
            startTimer();
        } else {
            cancelTimer();
            onPostResult(pResponse);
        }
    }

    @Override
    public void onRequestFail(Throwable e) {
        Timber.d(e != null ? e.getMessage() : "Exception");
        cancelTimer();
        showProgress(false);
        //can not get status response from server
        //maybe because networking is not stable or networking is off
        if (ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            askToRetryGetStatus(GlobalData.getStringResource(RS.string.zingpaysdk_alert_error_networking_ask_to_retry));
        } else if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            onPostResult(createReponse(-1, GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction)));
        } else {
            onPostResult(createReponse(-1, null));
        }
    }

    @Override
    public void onRequestInProcess() {
        showProgress(true);
        Timber.d("onRequestInProcess");
    }

    @Override
    public String getDefaulErrorNetwork() {
        return null;
    }

    @Override
    protected void doRequest() {
        startTime = System.currentTimeMillis();
        GetStatusShare.shared().onGetStatus(this, getDataParams());
    }

    @Override
    protected boolean doParams() {
        try {
            GetStatusShare.shared().onPrepareParamsGetStatus(String.valueOf(mAppId), mDataParams, mUserInfo, mTransID);
            return true;
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(e);
            return false;
        }
    }

}
