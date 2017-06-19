package vn.com.zalopay.wallet.ui.channellist;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.password.interfaces.IPinCallBack;
import com.zalopay.ui.widget.password.managers.PasswordManager;

import java.lang.ref.WeakReference;

import rx.Subscription;
import rx.functions.Action1;
import vn.com.zalopay.wallet.api.IRequest;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.PaymentState;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.exception.InvalidStateException;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.IBank;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.GetTransStatus;
import vn.com.zalopay.wallet.transaction.SubmitOrder;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

import static vn.com.zalopay.wallet.constants.Constants.MAX_RETRY_GETSTATUS;
import static vn.com.zalopay.wallet.constants.Constants.PMC_CONFIG;
import static vn.com.zalopay.wallet.constants.Constants.STATUS_RESPONSE;
import static vn.com.zalopay.wallet.helper.TransactionHelper.getSubmitExceptionMessage;
import static vn.com.zalopay.wallet.ui.channellist.ChannelListPresenter.REQUEST_CODE;

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
    private IRequest mRequestApi;
    private WeakReference<ChannelListPresenter> mChannelListPresenter;
    private StatusResponse mStatusResponse;
    private String mTransId = "0";
    private String fpPassword;//password from fingerprint
    private String inputPassword;//password input on popup
    private boolean transStatusStart = false;
    private int retryTransStatusCount = 1;
    private Action1<Throwable> submitOrderException = throwable -> {
        Log.d(this, "submit order on error", throwable);
        if (mStatusResponse == null) {
            mStatusResponse = new StatusResponse(-1, getSubmitExceptionMessage(GlobalData.getAppContext()));
        }
        mPaymentInfoHelper.setResult(PaymentStatus.FAILURE);
        moveToResultScreen();
    };
    private Action1<Throwable> transStatusException = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            try {
                askToRetryGetStatus();
            } catch (Exception e) {
                startChannelActivity();
            }
            Log.d(this, "trans status on error", throwable);
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
            Log.d(this, "user canceled password");
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
    private Action1<StatusResponse> transStatusSubscriber = new Action1<StatusResponse>() {
        @Override
        public void call(StatusResponse statusResponse) {
            Log.d(this, "get tran status on complete", statusResponse);
            if (mRequestApi.isRunning()) {
                Log.d(this, "get tran status is running - skip process");
                return;
            }
            processStatus(statusResponse);
        }
    };
    private Action1<StatusResponse> submitOrderSubscriber = statusResponse -> {
        Log.d(this, "submit order on complete", statusResponse);
        if (statusResponse != null) {
            mTransId = statusResponse.zptransid;
        }
        processStatus(statusResponse);
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
    }

    public static ChannelProxy get() {
        if (ChannelProxy._object == null) {
            ChannelProxy._object = new ChannelProxy();
        }
        return ChannelProxy._object;
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
            mPassword.setErrorMessage(RS.string.zpw_alert_network_error_submitorder);
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
                    if (transStatusStart && retryTransStatusCount < MAX_RETRY_GETSTATUS) {
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
                    if (mPassword != null) {
                        //user using poup , not fingerprint
                        mPassword.setErrorMessage(mStatusResponse.returnmessage);
                    }
                    Activity activity = BaseActivity.getCurrentActivity();
                    if (activity == null || activity.isFinishing()) {
                        return;
                    }
                    getPasswordManager(activity).showPinView();//show again if user using fingerprint
                    break;
            }
        }
    }

    private void moveToResultScreen() {
        //reset value to notify on fail screen
        if (TransactionHelper.isOrderProcessing(mStatusResponse)) {
            mStatusResponse.returncode = -1;
            mStatusResponse.returnmessage = GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_get_status_fail);
        }
        closePassword();
        startChannelActivity();
    }

    private void getTransStatus() {
        try {
            mPassword.showLoading(true);
            Subscription subscription = transStatus();
            getPresenter().addSubscription(subscription);
        } catch (Exception e) {
            Log.e(this, e);
            closePassword();
            startChannelActivity();
        }
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
        }
    }

    private Subscription transStatus() {
        Log.d(this, "start check order status");
        mRequestApi = getTransStatusRequest();
        return ((GetTransStatus) mRequestApi).getObserver()
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
        //balance less than order amount
        if (mChannel.isZaloPayChannel() && !mPaymentInfoHelper.balanceEnoughForPayment()) {
            mPaymentInfoHelper.setResult(PaymentStatus.ERROR_BALANCE);
            startChannelActivity();
        }
        //password flow
        else if (mChannel.isZaloPayChannel() || mChannel.isMapCardChannel() || mChannel.isBankAccountMap()) {
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
        //input flow
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
        try {
            getView().startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    private void startPasswordFlow(Activity pActivity) {
        try {
            if (PaymentFingerPrint.isDeviceSupportFingerPrint() && PaymentFingerPrint.isAllowFingerPrintFeature()) {
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
            mPassword = new PasswordManager(pActivity, mChannel.pmcname, logo_path, mPasswordCallback);
        } else {
            mPassword.setContent(mChannel.pmcname, logo_path);
        }
        return mPassword;
    }

    private IRequest getSubmitTransRequest() {
        ITransService transService = SDKApplication.getApplicationComponent().transService();
        return new SubmitOrder(transService,
                getPaymentInfoHelper().getAppId(), getPaymentInfoHelper().getUserInfo(),
                getPaymentInfoHelper().getLocation(), getPaymentInfoHelper().getTranstype());
    }

    private IRequest getTransStatusRequest() {
        ITransService transService = SDKApplication.getApplicationComponent().transService();
        return new GetTransStatus(transService,
                getPaymentInfoHelper().getAppId(), getPaymentInfoHelper().getUserInfo(), mTransId);
    }
}
