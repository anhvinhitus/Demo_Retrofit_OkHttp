package vn.com.zalopay.wallet.ui.channellist;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.password.interfaces.IPinCallBack;
import com.zalopay.ui.widget.password.managers.PasswordManager;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rx.Subscription;
import rx.functions.Action1;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.zalopay.utility.FingerprintUtils;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.api.IRequest;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentState;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkSuccessTransEvent;
import vn.com.zalopay.wallet.exception.InvalidStateException;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.IBank;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.StatusByAppTrans;
import vn.com.zalopay.wallet.transaction.SubmitOrder;
import vn.com.zalopay.wallet.transaction.TransStatus;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

import static vn.com.zalopay.wallet.constants.Constants.CHANNEL_PAYMENT_REQUEST_CODE;
import static vn.com.zalopay.wallet.constants.Constants.MAX_RETRY_GETSTATUS;
import static vn.com.zalopay.wallet.constants.Constants.PMC_CONFIG;
import static vn.com.zalopay.wallet.constants.Constants.RETRY_PASSWORD_MAX;
import static vn.com.zalopay.wallet.constants.Constants.STATUS_RESPONSE;
import static vn.com.zalopay.wallet.helper.TransactionHelper.getSubmitExceptionMessage;

/***
 * pre check before start payment channel
 */
public class ChannelProxy extends SingletonBase {
    private static ChannelProxy _object;
    private DialogFragment mFingerPrintDialog = null;
    private ChannelPreValidation mChannelPreValidation;
    private PasswordManager mPassword;
    private PaymentChannel mChannel;
    private PaymentInfoHelper mPaymentInfoHelper;
    private IBank mBankInteractor;
    private ITransService mTransService;
    private IRequest mRequestApi;
    private Subscription mSubscription;
    private WeakReference<ChannelListPresenter> mChannelListPresenter;
    private StatusResponse mStatusResponse;
    private String mTransId = "0";
    private String fpPassword;//password from fingerprint
    private String inputPassword;//password input on popup
    private boolean transStatusStart = false;
    private int showRetryDialogCount = 1;
    private int retryPassword = 1;
    private Action1<Throwable> appTransStatusException = throwable -> markTransFail(getSubmitExceptionMessage(GlobalData.getAppContext()));
    private Action1<StatusResponse> transStatusSubscriber = new Action1<StatusResponse>() {
        @Override
        public void call(StatusResponse statusResponse) {
            if (mRequestApi.isRunning()) {
                Log.d(this, "get tran status is running - skip process");
                return;
            }
            processStatus(statusResponse);
        }
    };
    private Action1<Throwable> transStatusException = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            if (networkException(throwable)) {
                return;
            }
            try {
                if (showRetryDialogCount < MAX_RETRY_GETSTATUS) {
                    askToRetryGetStatus();
                } else {
                    moveToResultScreen();
                }
            } catch (Exception e) {
                startChannelActivity();
            }
            Log.d(this, "trans status on error" + GsonUtils.toJsonString(throwable));
        }
    };
    private Action1<StatusResponse> appTransStatusSubscriber = new Action1<StatusResponse>() {
        @Override
        public void call(StatusResponse statusResponse) {
            if (mRequestApi.isRunning()) {
                Log.d(this, "get app tran status is running - skip process");
                return;
            }
            if (PaymentStatusHelper.isTransactionNotSubmit(statusResponse)) {
                markTransFail(getSubmitExceptionMessage(GlobalData.getAppContext()));
            } else {
                processStatus(statusResponse);
            }
        }
    };
    private Action1<Throwable> submitOrderException = throwable -> {
        Log.d(this, "submit order on error", throwable);
        if (!networkException(throwable)) {
            //check trans status by app trans id
            getTransStatusByAppTrans();
        }
    };

    private Action1<StatusResponse> submitOrderSubscriber = statusResponse -> {
        Log.d(this, "submit order on complete", statusResponse);
        if (statusResponse == null) {
            //check trans status by app trans id
            getTransStatusByAppTrans();
        } else {
            //continue check payment status
            mTransId = statusResponse.zptransid;
            processStatus(statusResponse);
        }
    };
    private IPinCallBack mPasswordCallback = new IPinCallBack() {
        @Override
        public void onError(String pError) {
            Log.e(this, pError);
        }

        @Override
        public void onCheckedFingerPrint(boolean pChecked) {
            Log.d(this, "on changed check", pChecked);
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onComplete(String pHashPassword) {
            if (preventSubmitOrder()) {
                Log.d(this, "order is submit - skip");
                return;
            }
            Log.d(this, "password", pHashPassword);
            if (!TextUtils.isEmpty(pHashPassword)) {
                Log.d(this, "start submit trans pw", pHashPassword);
                inputPassword = pHashPassword;
                submitOrder(pHashPassword);
            } else {
                Log.e(this, "empty password");
            }
        }
    };
    private final IFPCallback mFingerPrintCallback = new IFPCallback() {
        @Override
        public void onError(FPError pError) {
            dismissFingerPrintDialog();
            try {
                getView().showInfoDialog(GlobalData.getStringResource(RS.string.zpw_error_authen_pin));
            } catch (Exception e) {
                Log.d(this, e);
            }
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onComplete(String pHashPassword) {
            dismissFingerPrintDialog();
            if (preventSubmitOrder()) {
                Log.d(this, "order is submit - skip");
                return;
            }
            //user don't setting use fingerprint for payment
            if (TextUtils.isEmpty(pHashPassword)) {
                Activity activity = BaseActivity.getCurrentActivity();
                if (activity != null && !activity.isFinishing()) {
                    showPassword(activity);
                }
            } else {
                //submit password
                Log.d(this, "start submit trans pw", pHashPassword);
                fpPassword = pHashPassword;
                submitOrder(pHashPassword);
            }
        }
    };
    private Action1<BankConfigResponse> bankListSubscriber = new Action1<BankConfigResponse>() {
        @Override
        public void call(BankConfigResponse bankConfigResponse) {
            String bankCode = mPaymentInfoHelper.getMapBank().bankcode;
            if (!isBankMaintenance(bankCode) && isBankSupport(bankCode)) {
                startFlow();
            }
            try {
                getView().hideLoading();
            } catch (Exception e) {
                Log.d(this, e);
            }
        }
    };

    public ChannelProxy() {
        super();
        mTransService = SDKApplication.getApplicationComponent().transService();
    }

    public static ChannelProxy get() {
        if (ChannelProxy._object == null) {
            ChannelProxy._object = new ChannelProxy();
        }
        return ChannelProxy._object;
    }

    private boolean networkException(Throwable throwable) {
        boolean networkError = throwable instanceof NetworkConnectionException;
        if (networkError) {
            markTransFail(getSubmitExceptionMessage(GlobalData.getAppContext()));
        }
        return networkError;
    }

    private void markTransFail(String pError) {
        if (mStatusResponse == null) {
            mStatusResponse = new StatusResponse(-1, pError);
        } else {
            mStatusResponse.returnmessage = pError;
        }
        mPaymentInfoHelper.setResult(PaymentStatus.FAILURE);
        moveToResultScreen();
    }

    public List<Object> getChannels() throws Exception {
        return getPresenter().getChannelList();
    }

    private boolean preventSubmitOrder() {
        boolean isSubmitted = mRequestApi != null && mRequestApi.isRunning();
        if (isSubmitted) {
            try {
                getView().showInfoDialog(GlobalData.getStringResource(RS.string.sdk_warning_order_submit));
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        return isSubmitted;
    }

    private void closePassword() {
        if (mPassword != null) {
            mPassword.closePinView();
            mPassword = null;
        }
    }

    private void askToRetryGetStatus() throws Exception {
        showRetryDialogCount++;
        String message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_ask_to_retry);
        getView().showRetryDialog(message, new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                moveToResultScreen();
            }

            @Override
            public void onOKevent() {
                getTransStatus();
            }
        });
    }

    private void processStatus(StatusResponse pResponse) {
        try {
            getView().updateDefaultTitle();
        } catch (Exception e) {
            Log.e(this, e);
        }
        if (pResponse == null) {
            markTransFail(TransactionHelper.getGenericExceptionMessage(GlobalData.getAppContext()));
        } else {
            mStatusResponse = pResponse;
            if (TextUtils.isEmpty(mTransId)) {
                mTransId = mStatusResponse.zptransid;
            }
            @PaymentState int status = TransactionHelper.paymentState(mStatusResponse);
            switch (status) {
                case PaymentState.SUCCESS:
                    mPaymentInfoHelper.setResult(PaymentStatus.SUCCESS);
                    updatePasswordOnSuccess();
                    moveToResultScreen();
                    break;
                case PaymentState.SECURITY:
                    closePassword();
                    startChannelActivity();
                    break;
                case PaymentState.PROCESSING:
                    mPaymentInfoHelper.setResult(PaymentStatus.PROCESSING);
                    //continue get trans status
                    if (transStatusStart && showRetryDialogCount < MAX_RETRY_GETSTATUS) {
                        try {
                            askToRetryGetStatus();
                        } catch (Exception e) {
                            Log.e(this, e);
                            moveToResultScreen();
                        }
                    } else if (transStatusStart) {
                        moveToResultScreen();
                    } else {
                        getTransStatus();
                        transStatusStart = true;
                    }
                    break;
                case PaymentState.FAILURE:
                    mPaymentInfoHelper.setResult(PaymentStatus.FAILURE);
                    mPaymentInfoHelper.updateTransactionResult(mStatusResponse.returncode);
                    moveToResultScreen();
                    break;
                case PaymentState.INVALID_PASSWORD:
                    if (retryPassword >= RETRY_PASSWORD_MAX) {
                        moveToResultScreen();
                    } else {
                        setError(mStatusResponse.returnmessage);
                        retryPassword++;
                    }
                    break;
            }
        }
    }

    private void setError(String pError) {
        if (mPassword != null) {
            mPassword.setErrorMessage(pError);
            mPassword.unlock();
        }
    }

    private synchronized void moveToResultScreen() {
        //reset value to notify on fail screen
        if (TransactionHelper.isOrderProcessing(mStatusResponse)) {
            mStatusResponse.returncode = -1;
            mStatusResponse.returnmessage = GlobalData.getStringResource(RS.string.sdk_fail_trans_status);
        }
        closePassword();
        startChannelActivity();
    }

    private void updatePasswordOnSuccess() {
        if (!TextUtils.isEmpty(fpPassword) && !TextUtils.isEmpty(inputPassword) && !fpPassword.equals(inputPassword)) {
            try {
                PaymentFingerPrint.shared().updatePassword(fpPassword, inputPassword);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    public ChannelProxy setBankInteractor(IBank pBankInteractor) {
        mBankInteractor = pBankInteractor;
        return this;
    }

    public ChannelProxy setChannel(PaymentChannel pChannel) {
        mChannel = pChannel;
        return this;
    }

    public ChannelListFragment getView() throws Exception {
        return mChannelListPresenter.get().getViewOrThrow();
    }

    public ChannelListPresenter getPresenter() throws Exception {
        if (mChannelListPresenter == null || mChannelListPresenter.get() == null) {
            throw new InvalidStateException("invalid presenter state");
        }
        return mChannelListPresenter.get();
    }

    public ChannelProxy setChannelListPresenter(ChannelListPresenter presenter) {
        mChannelListPresenter = new WeakReference<>(presenter);
        return this;
    }

    public PaymentInfoHelper getPaymentInfoHelper() {
        return mPaymentInfoHelper;
    }

    public ChannelProxy setPaymentInfo(PaymentInfoHelper paymentInfoHelper) {
        mPaymentInfoHelper = paymentInfoHelper;
        return this;
    }

    public boolean validate(PaymentChannel channel) {
        try {
            if (mChannelPreValidation == null) {
                mChannelPreValidation = new ChannelPreValidation(mPaymentInfoHelper, mBankInteractor, getPresenter());
            }
            return mChannelPreValidation.validate(channel);
        } catch (Exception e) {
            Log.d(this, e);
        }
        return false;
    }

    private void submitOrder(String pHashPassword) {
        try {
            if (getPresenter().networkOffline()) {
                closePassword();
                return;
            }
            Log.d(this, "start submit order");
            mPassword.showLoading(true);
            mPassword.lock();
            String chargeInfo = mPaymentInfoHelper.getChargeInfo(null);
            mRequestApi = getSubmitTransRequest();
            Subscription subscription =
                    ((SubmitOrder) mRequestApi).channelId(mChannel.pmcid)
                            .order(mPaymentInfoHelper.getOrder())
                            .password(pHashPassword)
                            .chargeInfo(chargeInfo)
                            .getObserver()
                            .compose(SchedulerHelper.applySchedulers())
                            .doOnNext(statusResponse -> {
                                try {
                                    mPassword.showLoading(true);
                                    getView().setTitle(GlobalData.getStringResource(RS.string.zpw_string_alert_submit_order));
                                } catch (Exception e) {
                                    Log.e(this, e);
                                }
                            })
                            .subscribe(submitOrderSubscriber, submitOrderException);
            getPresenter().addSubscription(subscription);
        } catch (Exception e) {
            Log.e(this, e);
            markTransFail(TransactionHelper.getSubmitExceptionMessage(BaseActivity.getCurrentActivity()));
        }
    }

    private void getTransStatus() {
        try {
            mSubscription = transStatus();
            getPresenter().addSubscription(mSubscription);
        } catch (Exception e) {
            Log.e(this, e);
            markTransFail(TransactionHelper.getGenericExceptionMessage(BaseActivity.getCurrentActivity()));
        }
    }

    private void getTransStatusByAppTrans() {
        new Handler().postDelayed(() -> {
            try {
                mSubscription = appTransStatus();
                getPresenter().addSubscription(mSubscription);
            } catch (Exception e) {
                Log.e(this, e);
                markTransFail(TransactionHelper.getGenericExceptionMessage(BaseActivity.getCurrentActivity()));
            }
        }, 1000);
    }

    private Subscription transStatus() {
        Log.d(this, "start check order status");
        mRequestApi = getTransStatusRequest();
        return ((TransStatus) mRequestApi).getObserver()
                .compose(SchedulerHelper.applySchedulers())
                .doOnNext(statusResponse -> {
                    try {
                        getView().setTitle(GlobalData.getStringResource(RS.string.zingpaysdk_alert_checking));
                    } catch (Exception e) {
                        Log.e(this, e);
                    }
                })
                .subscribe(transStatusSubscriber, transStatusException);
    }

    private Subscription appTransStatus() {
        Log.d(this, "start app trans status");
        mRequestApi = getTransStatusAppTransRequest();
        return ((StatusByAppTrans) mRequestApi).getObserver()
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(appTransStatusSubscriber, appTransStatusException);
    }

    /***
     * start payment channel
     */
    public void start() throws Exception {
        Log.d(this, "start payment channel", mChannel);
        Activity activity = BaseActivity.getCurrentActivity();
        if (!(activity instanceof BaseActivity)) {
            Log.e(this, "channel list activity is not valid");
            return;
        }
        //map card channel clicked
        if (mChannel.isMapValid()) {
            BaseMap mapBank = mChannel.isBankAccountMap ? new BankAccount() : new MapCard();
            mapBank.setLastNumber(mChannel.l4no);
            mapBank.setFirstNumber(mChannel.f6no);
            mapBank.bankcode = mChannel.bankcode;
            mPaymentInfoHelper.paymentInfo.setMapBank(mapBank);
        } else {
            mPaymentInfoHelper.paymentInfo.setMapBank(null);
        }
        mPaymentInfoHelper.getOrder().populateFee(mChannel);
        if (mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) {
            getView().showLoading(GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
            ChannelListPresenter presenter = getPresenter();
            presenter.loadBankList(bankListSubscriber, presenter.mBankListException);
        } else {
            startFlow();
        }
    }

    private boolean isBankMaintenance(String pBankCode) {
        if (GlobalData.getCurrentBankFunction() == BankFunctionCode.PAY) {
            GlobalData.getBankFunctionPay(mPaymentInfoHelper);
        }

        int bankFunction = GlobalData.getCurrentBankFunction();
        BankConfig bankConfig = mBankInteractor.getBankConfig(pBankCode);
        if (bankConfig != null && bankConfig.isBankMaintenence(bankFunction)) {
            try {
                getView().showInfoDialog(bankConfig.getMaintenanceMessage(bankFunction));
                return true;
            } catch (Exception e) {
                Log.d(this, e);
            }
        }
        return false;
    }

    private boolean isBankSupport(String pBankCode) {
        BankConfig bankConfig = SDKApplication.getApplicationComponent().bankListInteractor().getBankConfig(pBankCode);
        if (bankConfig == null || !bankConfig.isActive()) {
            String message = GlobalData.getStringResource(RS.string.zpw_string_bank_not_support);
            try {
                getView().showInfoDialog(message);
            } catch (Exception e) {
                Log.d(this, e);
            }
            return false;
        }
        return true;
    }

    private void startFlow() {
        Activity activity = BaseActivity.getCurrentActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        //password flow
        if (mChannel.isZaloPayChannel() || mChannel.isMapCardChannel() || mChannel.isBankAccountMap()) {
            if (TransactionHelper.needUserPasswordPayment(mChannel, mPaymentInfoHelper.getOrder())) {
                startPasswordFlow(activity);
            } else {
                //submit order without password
                submitOrder("");
            }
        } else {
            //input card info flow
            startChannelActivity();
        }
    }

    private void startChannelActivity() {
        Lock lock = new ReentrantLock();
        try {
            lock.lock();
            Intent intent = new Intent(GlobalData.getAppContext(), PaymentChannelActivity.class);
            if (mStatusResponse != null) {
                /***
                 * re-assign trans id again because of some case
                 * trans id = null when get status
                 */
                mStatusResponse.zptransid = mTransId;
                intent.putExtra(STATUS_RESPONSE, mStatusResponse);
                Log.d(this, "start channel status response", mStatusResponse);
            }
            intent.putExtra(PMC_CONFIG, mChannel);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            getView().startActivityForResult(intent, CHANNEL_PAYMENT_REQUEST_CODE);
        } catch (Exception e) {
            Log.e(this, e);
        } finally {
            lock.unlock();
        }
    }

    private void startPasswordFlow(Activity pActivity) {
        try {
            if (FingerprintUtils.deviceSupportFingerPrint(pActivity.getApplicationContext()) && PaymentFingerPrint.isAllowFingerPrintFeature()) {
                showFingerPrint(pActivity);
            } else {
                showPassword(pActivity);
            }
        } catch (Exception ex) {
            showPassword(pActivity);
            Log.e(this, ex);
        }
    }

    private void showPassword(Activity pActivity) {
        getPasswordManager(pActivity).showPinView();
    }

    private void showFingerPrint(Activity pActivity) throws Exception {
        mFingerPrintDialog = PaymentFingerPrint.shared().getDialogFingerprintAuthentication(pActivity, mFingerPrintCallback);
        if (mFingerPrintDialog != null) {
            mFingerPrintDialog.show(pActivity.getFragmentManager(), null);
        } else {
            showPassword(pActivity);
            Log.d(this, "use password instead of fingerprint");
        }
    }

    private void dismissFingerPrintDialog() {
        if (mFingerPrintDialog != null && !mFingerPrintDialog.isDetached()) {
            mFingerPrintDialog.dismiss();
            mFingerPrintDialog = null;
            Log.d(this, "dismiss dialog fingerprint");
        }
    }

    private PasswordManager getPasswordManager(Activity pActivity) {
        String logo_path = ResourceManager.getAbsoluteImagePath(mChannel.channel_icon);
        if (mPassword == null) {
            boolean supportFingerPrint = FingerprintUtils.deviceSupportFingerPrint(pActivity.getApplicationContext());
            mPassword = new PasswordManager(pActivity, mChannel.pmcname, logo_path, supportFingerPrint, mPasswordCallback);
        } else {
            mPassword.setContent(mChannel.pmcname, logo_path);
        }
        return mPassword;
    }

    private IRequest getSubmitTransRequest() {
        return new SubmitOrder(mTransService,
                getPaymentInfoHelper().getAppId(), getPaymentInfoHelper().getUserInfo(),
                getPaymentInfoHelper().getLocation(), getPaymentInfoHelper().getTranstype());
    }

    private IRequest getTransStatusRequest() {
        return new TransStatus(mTransService,
                getPaymentInfoHelper().getAppId(), getPaymentInfoHelper().getUserInfo(), mTransId);
    }

    private IRequest getTransStatusAppTransRequest() {
        Log.d(this, "start check order by app trans id");
        return new StatusByAppTrans(mTransService,
                getPaymentInfoHelper().getAppId(), getPaymentInfoHelper().getUserId(), getPaymentInfoHelper().getAppTransId());
    }

    public void OnTransEvent(Object... pEventData) throws Exception {
        Log.d(this, "on trans event", pEventData);
        if (pEventData == null || pEventData.length < 2) {
            Log.d(this, "trans event invalid");
            return;
        }
        EEventType event_type = (EEventType) pEventData[0];
        switch (event_type) {
            case ON_NOTIFY_TRANSACTION_FINISH:
                if (pEventData[1] instanceof SdkSuccessTransEvent) {
                    processSuccessTransNotification((SdkSuccessTransEvent) pEventData[1]);
                }
                break;
        }
    }

    private void processSuccessTransNotification(SdkSuccessTransEvent pEvent) {
        if (!Constants.TRANSACTION_SUCCESS_NOTIFICATION_TYPES.contains(pEvent.notification_type)) {
            Log.d(this, "notification type is not accepted for this kind of transaction", pEvent.notification_type);
            return;
        }
        if (pEvent.transid != Long.parseLong(mTransId)) {
            Log.d(this, "invalid trans id");
            return;
        }
        //make sure api get trans status is running
        if (mRequestApi instanceof TransStatus && mRequestApi.isRunning()) {
            //cancel running request
            if (mSubscription != null && !mSubscription.isUnsubscribed()) {
                mSubscription.unsubscribe();
                Log.d(this, "cancel api trans status");
            }
            if (mPaymentInfoHelper.isMoneyTranferTrans() && pEvent.trans_time > 0) {
                mPaymentInfoHelper.getOrder().apptime = pEvent.trans_time;
                Log.d(this, "update transaction time from notification");
            }
            //mark trans as success
            mPaymentInfoHelper.setResult(PaymentStatus.SUCCESS);
            updatePasswordOnSuccess();
            moveToResultScreen();
            Log.d(this, "trans success from notification");
        }
    }
}
