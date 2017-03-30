package vn.com.zalopay.wallet.datasource.task.getstatus;

import android.app.Activity;
import android.os.CountDownTimer;
import android.text.TextUtils;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.SecurityResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;

/***
 * get transaction status class
 */
public class GetStatus extends BaseTask<StatusResponse> {
    protected static CountDownTimer mTimer;//coundown timer to retry get status
    private String mTransID;
    private boolean mIsNeedToCheckDataInResponse;
    private AdapterBase mAdapter;
    private boolean isTimerStated = false;
    private String mMessage;
    private int mRetryCount = 1;

    public GetStatus(AdapterBase pAdapter, String pTransID, boolean pIsCheckData, String pMessage) {
        super();
        this.mTransID = pTransID;
        this.mIsNeedToCheckDataInResponse = pIsCheckData;
        this.mAdapter = pAdapter;
        this.mMessage = pMessage;
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
        if (GlobalData.isZalopayChannel())
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
            Log.d(this, "user in fail screen, don't need show retry dialog again");
            return;
        }

        Activity activity = BasePaymentActivity.getCurrentActivity();
        if (activity == null || activity.isFinishing() || !(activity instanceof BasePaymentActivity)) {
            onPostResult(createReponse(-1, GlobalData.getStringResource(GlobalData.getTransProcessingMessage())));
            return;
        }

        if (mRetryCount == Constants.MAX_RETRY_GETSTATUS) {
            onPostResult(createReponse(-1, GlobalData.getStringResource(GlobalData.getTransProcessingMessage())));
            return;
        }

        ((BasePaymentActivity) activity).showRetryDialog(new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                onPostResult(createReponse(-1, GlobalData.getStringResource(GlobalData.getTransProcessingMessage())));
            }

            @Override
            public void onOKevent() {
                mRetryCount++;
                makeRequest();
            }

        }, pMessage);
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
        BasePaymentActivity activity = mAdapter.getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.showProgress(pIsShow, mMessage);
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
        if (!(pResponse instanceof StatusResponse)) {
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
            pResponse.returnmessage = GlobalData.getStringResource(GlobalData.getTransProcessingMessage());

            mAdapter.setLoadWebTimeout(false);
            cancelTimer();
            onPostResult(pResponse);
            Log.d(this, "load website timeout");
            return;
        }
        //flow 3ds
        if (mIsNeedToCheckDataInResponse && !TextUtils.isEmpty(pResponse.data)) {
            try {
                SecurityResponse dataResponse = GsonUtils.fromJsonString(pResponse.data, SecurityResponse.class);
                if (dataResponse != null && PaymentStatusHelper.is3DSResponse(dataResponse) || PaymentStatusHelper.isOtpResponse(dataResponse)) {
                    cancelTimer();
                    onPostResult(pResponse);
                    return;
                }
            } catch (Exception ex) {
                Log.e(this, ex);
                cancelTimer();
                onPostResult(createReponse(-1, null));
            }

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
        Log.d(this, e);
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
        Log.d(this, "onRequestInProcess");
    }

    @Override
    public String getDefaulErrorNetwork() {
        return null;
    }

    @Override
    protected void doRequest() {
        GetStatusShare.shared().onGetStatus(this, getDataParams());
    }

    @Override
    protected boolean doParams() {
        try {
            GetStatusShare.shared().onPrepareParamsGetStatus(mDataParams, mTransID);
            return true;
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(e);
            return false;
        }
    }

}
