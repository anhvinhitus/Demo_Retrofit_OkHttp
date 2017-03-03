package vn.com.zalopay.wallet.datasource.request.getstatus;

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
import vn.com.zalopay.wallet.datasource.request.BaseRequest;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;

/***
 * get transaction status class
 */
public class GetStatus extends BaseRequest<StatusResponse> {
    private String mTransID;
    private boolean mIsNeedToCheckDataInResponse;
    private AdapterBase mAdapter;
    private boolean mRetry;

    //coundown timer to retry get status
    private CountDownTimer mTimer;
    private boolean isTimerStated = false;
    private String mMessage;
    private int mRetryCount = 1;

    /***
     * constructor
     *
     * @param pAdapter
     * @param pTransID
     * @param pIsCheckData
     */
    public GetStatus(AdapterBase pAdapter, String pTransID, boolean pIsCheckData, boolean pRetry, String pMessage) {
        super();
        this.mTransID = pTransID;
        this.mIsNeedToCheckDataInResponse = pIsCheckData;
        this.mAdapter = pAdapter;
        this.mRetry = pRetry;
        this.mMessage = pMessage;
        initTimer();
    }

    private void initTimer() {
        //reduce double to zalopay channel
        int intervalRetry = Constants.SLEEPING_INTERVAL_OF_RETRY;
        if (GlobalData.isZalopayChannel())
            intervalRetry /= 2;

        mTimer = new CountDownTimer(Constants.MAX_INTERVAL_OF_RETRY, intervalRetry) {
            public void onTick(long millisUntilFinished) {
                mRetry = true;
                makeRequest();
                Log.d(this, "===starting retry get status===");
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

        Activity activity = BasePaymentActivity.getCurrentActivity();

        if (activity == null || activity.isFinishing() || !(activity instanceof BasePaymentActivity)) {
            onRequestFail(GlobalData.getStringResource(GlobalData.getTransProcessingMessage()));
            return;
        }

        if (mRetryCount == Constants.MAX_RETRY_GETSTATUS) {
            onRequestFail(GlobalData.getStringResource(GlobalData.getTransProcessingMessage()));
            return;
        }

        ((BasePaymentActivity) activity).showRetryDialog(new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                onRequestFail(GlobalData.getStringResource(GlobalData.getTransProcessingMessage()));
            }

            @Override
            public void onOKevent() {
                showProgress(true);

                mRetry = true;
                mRetryCount++;
                makeRequest();
            }

        }, pMessage);
    }

    private void onPostResult() {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_GET_STATUS_COMPLETE, getResponse());
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

    @Override
    protected void onRequestSuccess() throws Exception {
        if (!(getResponse() instanceof StatusResponse)) {
            cancelTimer();
            askToRetryGetStatus(GlobalData.getStringResource(RS.string.zingpaysdk_alert_error_networking_ask_to_retry));
            return;
        }

        //getResponse().data = "{\"actiontype\":1,\"redirecturl\":\"ac2pl\"}";
        //getResponse().isprocessing = true;
        //stop get status right away if wrong otp
        if (PaymentStatusHelper.isWrongOtpResponse(getResponse())) {
            cancelTimer();
            onPostResult();
            return;
        }
        //flow retry oneshot if load bank's website timeout
        if (mAdapter.isLoadWebTimeout() && getResponse() != null && getResponse().isprocessing) {
            getResponse().isprocessing = false;
            getResponse().returncode = -1;
            getResponse().returnmessage = GlobalData.getStringResource(GlobalData.getTransProcessingMessage());

            mAdapter.setLoadWebTimeout(false);

            Log.d(this, "Load website timeout,reset result");
        }
        //flow 3ds
        if (mIsNeedToCheckDataInResponse && getResponse() != null && !TextUtils.isEmpty(getResponse().data)) {
            SecurityResponse dataResponse = GsonUtils.fromJsonString(getResponse().data, SecurityResponse.class);

            if (PaymentStatusHelper.is3DSResponse(dataResponse) || PaymentStatusHelper.isOtpResponse(dataResponse)) {
                cancelTimer();
                onPostResult();
                return;
            }
        }
        //continue get status  because order still is processing
        else if (getResponse() != null && getResponse().isprocessing) {
            showProgress(true);
            startTimer();
        } else {
            cancelTimer();
            onPostResult();
        }

    }

    @Override
    protected void onRequestFail(String pMessage) {
        Log.d(this, "===onRequestFail===pMessage=" + pMessage);

        cancelTimer();

        //can not get status response from server
        //maybe because networking is not stable or networking is off
        if (TextUtils.isEmpty(pMessage) && ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            askToRetryGetStatus(GlobalData.getStringResource(RS.string.zingpaysdk_alert_error_networking_ask_to_retry));
            return;
        } else if (TextUtils.isEmpty(pMessage) && !ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            createReponse(-1, GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction));
        } else if (!TextUtils.isEmpty(pMessage)) {
            createReponse(-1, pMessage);
        }

        onPostResult();
    }

    @Override
    protected void createReponse(int pCode, String pMessage) {
        if (getResponse() == null) {
            mResponse = new StatusResponse(pCode, pMessage);
        }
        mResponse.isprocessing = false;
        mResponse.returnmessage = pMessage;
    }

    @Override
    protected void onRequestInProcess() {

    }

    @Override
    protected void doRequest() {
        Log.d(this, "===Begin getting status transID: " + mTransID);

        try {
            GetStatusShare.shared().onGetStatus(getDataSourceListener(), getDataParams(), mRetry);
        } catch (Exception ex) {
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
        }
    }

    @Override
    protected boolean doParams() {
        try {
            GetStatusShare.shared().onPrepareParamsGetStatus(mDataParams, mTransID);
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }
        return true;
    }

}
