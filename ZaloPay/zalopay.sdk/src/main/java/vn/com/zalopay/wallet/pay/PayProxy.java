package vn.com.zalopay.wallet.pay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.IRequest;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.OrderState;
import vn.com.zalopay.wallet.constants.PaymentState;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkSuccessTransEvent;
import vn.com.zalopay.wallet.exception.InvalidStateException;
import vn.com.zalopay.wallet.helper.ChannelHelper;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.IBank;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.StatusByAppTrans;
import vn.com.zalopay.wallet.transaction.SubmitOrder;
import vn.com.zalopay.wallet.transaction.TransStatus;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
import vn.com.zalopay.wallet.ui.channellist.ChannelListFragment;
import vn.com.zalopay.wallet.ui.channellist.ChannelListPresenter;

import static vn.com.zalopay.wallet.constants.Constants.CHANNEL_PAYMENT_REQUEST_CODE;
import static vn.com.zalopay.wallet.constants.Constants.MAX_RETRY_GETSTATUS;
import static vn.com.zalopay.wallet.constants.Constants.PMC_CONFIG;
import static vn.com.zalopay.wallet.constants.Constants.RETRY_PASSWORD_MAX;
import static vn.com.zalopay.wallet.constants.Constants.STATUS_RESPONSE;
import static vn.com.zalopay.wallet.helper.TransactionHelper.getGenericExceptionMessage;
import static vn.com.zalopay.wallet.helper.TransactionHelper.getSubmitExceptionMessage;

/***
 * pre check before start payment channel
 */
public class PayProxy extends SingletonBase {
    private static PayProxy _object;
    Context mContext;
    private WeakReference<BaseActivity> mActivity;
    private ValidationActor mValidActor;
    private AuthenActor mAuthenActor;
    private PaymentChannel mChannel;
    private PaymentInfoHelper mPaymentInfoHelper;
    private ITransService mTransService;
    private IRequest mRequestApi;
    private Subscription mSubscription;
    private WeakReference<ChannelListPresenter> mChannelListPresenter;
    private StatusResponse mStatusResponse;
    private IBank mBankInteractor;
    private String mTransId = "0";
    private boolean transStatusStart = false;
    private int showRetryDialogCount = 1;
    private int retryPassword = 1;
    private Action1<Throwable> appTransStatusException = throwable -> markTransFail(getSubmitExceptionMessage(mContext));
    private Action1<Throwable> transStatusException = throwable -> {
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
        Timber.d(throwable, "trans status on error");
    };
    private Action1<StatusResponse> transStatusSubscriber = statusResponse -> {
        try {
            processStatus(statusResponse);
        } catch (Exception e) {
            Log.e(this, e);
            markTransFail(getGenericExceptionMessage(mContext));
        }
    };
    private Action1<StatusResponse> appTransStatusSubscriber = statusResponse -> {
        if (PaymentStatusHelper.isTransactionNotSubmit(statusResponse)) {
            markTransFail(mContext.getString(R.string.sdk_error_not_submit_order));
        } else {
            try {
                processStatus(statusResponse);
            } catch (Exception e) {
                Log.e(this, e);
                markTransFail(getGenericExceptionMessage(mContext));
            }
        }
    };

    private PayProxy() {
        super();
    }

    ;

    public static PayProxy get() throws Exception {
        if (PayProxy._object == null) {
            throw new IllegalAccessException("invalid pay proxy");
        }
        return PayProxy._object;
    }

    public static PayProxy shared() {
        if (PayProxy._object == null) {
            PayProxy._object = new PayProxy();
        }
        return PayProxy._object;
    }

    private void onOrderSubmitedFailed(Throwable throwable) {
        Log.d(this, "submit order on error", throwable);
        if (!networkException(throwable)) {
            //check trans status by app trans id
            getTransStatusByAppTrans();
        }
    }

    private void onOrderSubmittedSuccess(StatusResponse statusResponse) {
        Log.d(this, "submit order on complete", statusResponse);
        if (statusResponse == null) {
            //check trans status by app trans id
            getTransStatusByAppTrans();
        } else {
            //continue check payment status
            mTransId = statusResponse.zptransid;
            try {
                processStatus(statusResponse);
            } catch (Exception e) {
                Log.e(this, e);
                markTransFail(getGenericExceptionMessage(mContext));
            }
        }
    }

    public PayProxy initialize(BaseActivity activity) {
        mActivity = new WeakReference<>(activity);
        mAuthenActor = AuthenActor.get().plant(this);
        mTransService = SDKApplication.getApplicationComponent().transService();
        mContext = SDKApplication.getApplication();
        mBankInteractor = SDKApplication.getApplicationComponent().bankListInteractor();
        return this;
    }

    public AuthenActor getAuthenActor() {
        return mAuthenActor;
    }

    public BaseActivity getActivity() throws Exception {
        if (mActivity.get() == null || mActivity.get().isFinishing()) {
            throw new IllegalStateException("invalid channel list activity");
        }
        return mActivity.get();
    }

    private boolean networkException(Throwable throwable) {
        boolean networkError = throwable instanceof NetworkConnectionException;
        if (networkError) {
            markTransFail(getSubmitExceptionMessage(mContext));
        }
        return networkError;
    }

    void markTransFail(String pError) {
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
                getView().showInfoDialog(mContext.getString(R.string.sdk_warning_order_submit));
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        return isSubmitted;
    }

    private void askToRetryGetStatus() throws Exception {
        showRetryDialogCount++;
        String message = mContext.getString(R.string.zingpaysdk_alert_processing_ask_to_retry);
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

    private void processStatus(StatusResponse pResponse) throws Exception {
        Log.d(this, "process status", pResponse);
        try {
            getView().updateDefaultTitle();
        } catch (Exception e) {
            Log.e(this, e);
        }
        if (pResponse == null) {
            markTransFail(TransactionHelper.getGenericExceptionMessage(mContext));
            return;

        }

        mStatusResponse = pResponse;
        if (TextUtils.isEmpty(mTransId) || mTransId.equals("0")) {
            mTransId = mStatusResponse.zptransid;
        }
        @PaymentState int status = TransactionHelper.paymentState(mStatusResponse);
        switch (status) {
            case PaymentState.SUCCESS:
                mPaymentInfoHelper.setResult(PaymentStatus.SUCCESS);
                moveToResultScreen();
                break;
            case PaymentState.SECURITY:
                mAuthenActor.closeAuthen();
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
                    showPassword(getActivity());
                    setError(mStatusResponse.returnmessage);
                    getView().updateDefaultTitle();
                    retryPassword++;
                }
                break;
        }
    }

    private void setError(String pError) {
        hideLoading(pError);
    }

    synchronized void moveToResultScreen() {
        //reset value to notify on fail screen
        if (TransactionHelper.isOrderProcessing(mStatusResponse)) {
            mStatusResponse.returncode = -1;
            mStatusResponse.returnmessage = mContext.getString(R.string.sdk_fail_trans_status);
        }
        mAuthenActor.closeAuthen();
        startChannelActivity();
    }

    public PayProxy setChannel(PaymentChannel pChannel) {
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

    public PayProxy setChannelListPresenter(ChannelListPresenter presenter) {
        mChannelListPresenter = new WeakReference<>(presenter);
        return this;
    }

    public PaymentInfoHelper getPaymentInfoHelper() {
        return mPaymentInfoHelper;
    }

    public PayProxy setPaymentInfo(PaymentInfoHelper paymentInfoHelper) {
        mPaymentInfoHelper = paymentInfoHelper;
        return this;
    }

    public boolean validate(PaymentChannel channel) {
        try {
            if (mValidActor == null) {
                mValidActor = new ValidationActor(mPaymentInfoHelper, mBankInteractor, getPresenter());
            }
            return mValidActor.validate(channel);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        return false;
    }

    private void showLoading(String pMessage) {
        try {
            if (mAuthenActor.showLoading()) {
                getView().setTitle(pMessage);
            } else {
                getView().showLoading(pMessage);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    private void hideLoading(String pError) {
        try {
            getView().setTitle(mPaymentInfoHelper.getTitleByTrans(GlobalData.getAppContext()));
            if (!mAuthenActor.hideLoading(pError)) {
                getView().hideLoading();
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    private void submitOrder(String pHashPassword) {
        try {
            if (getPresenter().networkOffline()) {
                mAuthenActor.closeAuthen();
                return;
            }
            Timber.d("start submit order");
            int channelId = mPaymentInfoHelper.isWithDrawTrans() ? BuildConfig.channel_zalopay : mChannel.pmcid;
            String chargeInfo = mPaymentInfoHelper.getChargeInfo(null);
            mRequestApi = getSubmitTransRequest();
            Subscription subscription =
                    ((SubmitOrder) mRequestApi).channelId(channelId)
                            .order(mPaymentInfoHelper.getOrder())
                            .password(pHashPassword)
                            .chargeInfo(chargeInfo)
                            .getObserver()
                            .compose(SchedulerHelper.applySchedulers())
                            .doOnNext(statusResponse -> showLoading(mContext.getString(R.string.zpw_string_alert_submit_order)))
                            .subscribe(this::onOrderSubmittedSuccess, this::onOrderSubmitedFailed);
            getPresenter().addSubscription(subscription);
        } catch (Exception e) {
            Log.e(this, e);
            markTransFail(TransactionHelper.getSubmitExceptionMessage(mContext));
        }
    }

    private void getTransStatus() {
        try {
            mSubscription = transStatus();
            getPresenter().addSubscription(mSubscription);
        } catch (Exception e) {
            Log.e(this, e);
            markTransFail(TransactionHelper.getGenericExceptionMessage(mContext));
        }
    }

    private void getTransStatusByAppTrans() {
        try {
            mSubscription = appTransStatus();
            getPresenter().addSubscription(mSubscription);
        } catch (Exception e) {
            Log.e(this, e);
            markTransFail(TransactionHelper.getGenericExceptionMessage(mContext));
        }
    }

    private Subscription transStatus() {
        Timber.d("start check order status");
        mRequestApi = getTransStatusRequest();
        return ((TransStatus) mRequestApi).getObserver()
                .compose(SchedulerHelper.applySchedulers())
                .doOnNext(statusResponse -> {
                    try {
                        getView().setTitle(mContext.getString(R.string.zingpaysdk_alert_checking));
                    } catch (Exception e) {
                        Log.e(this, e);
                    }
                })
                .subscribe(transStatusSubscriber, transStatusException);
    }

    private Subscription appTransStatus() {
        Timber.d("start app trans status");
        mRequestApi = getTransStatusAppTransRequest();
        return ((StatusByAppTrans) mRequestApi).getObserver()
                .compose(SchedulerHelper.applySchedulers())
                .doOnNext(statusResponse -> {
                    try {
                        getView().setTitle(mContext.getString(R.string.zingpaysdk_alert_checking));
                    } catch (Exception e) {
                        Log.e(this, e);
                    }
                })
                .subscribe(appTransStatusSubscriber, appTransStatusException);
    }

    /***
     * start payment channel
     */
    public void start() throws Exception {
        Log.d(this, "start payment channel", mChannel);
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
        mPaymentInfoHelper.getOrder().plusChannelFee(mChannel.totalfee);

        if (!mPaymentInfoHelper.payByCardMap() && !mPaymentInfoHelper.payByBankAccountMap()) {
            startFlow();
        } else {
            String bankCode = mPaymentInfoHelper.getMapBank().bankcode;
            if (!isBankMaintenance(bankCode) && isBankSupport(bankCode)) {
                startFlow();
            }
        }
    }

    private boolean isBankMaintenance(String pBankCode) {
        if (GlobalData.shouldUpdateBankFuncbyPayType()) {
            GlobalData.updateBankFuncByPayType();
        }

        int bankFunction = GlobalData.getCurrentBankFunction();
        BankConfig bankConfig = mBankInteractor.getBankConfig(pBankCode);
        if (bankConfig == null || !bankConfig.isBankMaintenence(bankFunction)) {
            return false;
        }

        try {
            getView().showInfoDialog(bankConfig.getMaintenanceMessage(bankFunction));
            return true;
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        return false;
    }

    private boolean isBankSupport(String pBankCode) {
        BankConfig bankConfig = mBankInteractor.getBankConfig(pBankCode);
        if (bankConfig != null && bankConfig.isActive()) {
            return true;
        }

        String message = GlobalData.getStringResource(RS.string.zpw_string_bank_not_support);
        try {
            getView().showInfoDialog(message);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        return false;
    }

    private void startFlow() throws Exception {
        //password flow
        if (!mChannel.isZaloPayChannel() && !mChannel.isMapCardChannel() && !mChannel.isBankAccountMap()) {
            startChannelActivity();
            return;
        }

        if (TransactionHelper.needUserPasswordPayment(mChannel, mPaymentInfoHelper.getOrder())) {
            startPasswordFlow(getActivity());
            return;
        }

        submitOrder("");
    }

    private boolean shouldCloseChannelList() {
        switch (mPaymentInfoHelper.getStatus()) {
            case PaymentStatus.SUCCESS:
            case PaymentStatus.FAILURE:
                return true;
            default:
                return mStatusResponse != null;
        }
    }

    private void startChannelActivity() {
        Lock lock = new ReentrantLock();
        try {
            lock.lock();
            Intent intent = new Intent(GlobalData.getAppContext(), ChannelActivity.class);
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
            intent.putExtra(Constants.CHANNEL_CONST.layout, ChannelHelper.getLayout(mChannel.pmcid, mPaymentInfoHelper.bankAccountLink()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            getView().startActivityForResult(intent, CHANNEL_PAYMENT_REQUEST_CODE);

            if (shouldCloseChannelList()) {
                getView().terminate();
            }
        } catch (Exception e) {
            Log.e(this, e);
        } finally {
            lock.unlock();
        }
    }

    private void startPasswordFlow(Activity pActivity) {
        try {

            boolean isShowFingerprint = showFingerPrint(pActivity);
            if (!isShowFingerprint) {
                showPassword(pActivity);
            }

        } catch (Exception ex) {
            showPassword(pActivity);
            Log.e(this, ex);
        }
    }

    void showPassword() {
        try {
            showPassword(getActivity());
        } catch (Exception e) {
            Log.e(this, e);
            markTransFail(getGenericExceptionMessage(mContext));
        }
    }

    void showPassword(Activity pActivity) {
        try {
            mAuthenActor.showPasswordPopup(pActivity, mChannel);
        } catch (Exception e) {
            Log.e(this, e);
            markTransFail(getGenericExceptionMessage(mContext));
        }
    }

    private boolean showFingerPrint(Activity pActivity) {
        try {
            return mAuthenActor.showFingerPrint(pActivity);
        } catch (Exception e) {
            Log.e(this, e);
            Timber.d("use password instead of fingerprint");
            return false;
        }
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
        Timber.d("start check order by app trans id");
        return new StatusByAppTrans(mTransService,
                getPaymentInfoHelper().getAppId(), getPaymentInfoHelper().getUserId(), getPaymentInfoHelper().getAppTransId());
    }

    public void OnTransEvent(Object... pEventData) throws Exception {
        Log.d(this, "on trans event", pEventData);
        if (pEventData == null || pEventData.length < 2) {
            Timber.d("trans event invalid");
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
            Timber.d("invalid trans id");
            return;
        }
        //make sure api get trans status is running
        if (!(mRequestApi instanceof TransStatus) || !mRequestApi.isRunning()) {
            return;
        }

        //cancel running request
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            Timber.d("cancel api trans status");
        }
        if (mPaymentInfoHelper.isMoneyTranferTrans() && pEvent.trans_time > 0) {
            mPaymentInfoHelper.getOrder().apptime = pEvent.trans_time;
            Timber.d("update transaction time from notification");
        }
        //mark trans as success
        mPaymentInfoHelper.setResult(PaymentStatus.SUCCESS);
        moveToResultScreen();
        Timber.d("trans success from notification");
    }

    public
    @OrderState
    int orderProcessing() {
        if (mRequestApi instanceof SubmitOrder && mRequestApi.isRunning()) {
            return OrderState.SUBMIT;
        } else if ((mRequestApi instanceof TransStatus || mRequestApi instanceof StatusByAppTrans) && mRequestApi.isRunning()) {
            return OrderState.QUERY_STATUS;
        } else {
            return OrderState.NO_STATUS;
        }
    }

    public void onCompletePasswordPopup(String pHashPassword) {
        if (preventSubmitOrder()) {
            Timber.d("order is submit - skip");
            return;
        }
        if (!TextUtils.isEmpty(pHashPassword)) {
            Log.d(this, "start submit trans pw", pHashPassword);
            submitOrder(pHashPassword);
        } else {
            Log.e(this, "empty password");
        }
    }

    public void onErrorPasswordPopup() {
        markTransFail(getGenericExceptionMessage(mContext));
    }

    public void onComleteFingerPrint(String pHashPassword) {
        if (preventSubmitOrder()) {
            Timber.d("order is submit - skip");
            return;
        }
        //user don't setting use fingerprint for payment
        if (TextUtils.isEmpty(pHashPassword)) {
            try {
                showPassword(getActivity());
            } catch (Exception e) {
                Log.e(this, e);
                markTransFail(getGenericExceptionMessage(mContext));
            }
        } else {
            //submit password
            Log.d(this, "start submit trans pw", pHashPassword);
            submitOrder(pHashPassword);
        }
    }

    public void onErrorFingerPrint() {
        try {
            getView().showInfoDialog(mContext.getString(R.string.zpw_error_authen_pin), new ZPWOnEventDialogListener() {
                @Override
                public void onOKevent() {
                    try {
                        showPassword(getActivity());
                    } catch (Exception e) {
                        Log.e(this, e);
                        markTransFail(getGenericExceptionMessage(mContext));
                    }
                }
            });
        } catch (Exception e) {
            Log.e(this, e);
            markTransFail(getGenericExceptionMessage(mContext));
        }
    }

    public void release() {
        Timber.d("start release pay proxy factors");
        mActivity = null;
        mValidActor = null;
        mRequestApi = null;
        mChannelListPresenter = null;
        mChannel = null;
        if (mAuthenActor != null) {
            mAuthenActor.release();
            mAuthenActor = null;
        }
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    public void resetResponse() {
        mStatusResponse = null;
    }
}
