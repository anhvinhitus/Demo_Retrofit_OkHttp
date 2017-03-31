package vn.com.vng.zalopay.service;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import rx.Subscription;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.eventbus.NewSessionEvent;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.business.entity.error.CError;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;

/**
 * Created by huuhoa on 12/8/16.
 * Listener for callback from SDK
 */
class WalletListener implements ZPPaymentListener {
    private PaymentWrapper mPaymentWrapper;
    private final TransactionStore.Repository mTransactionRepository;
    private final BalanceStore.Repository mBalanceRepository;

    WalletListener(PaymentWrapper paymentWrapper,
                   TransactionStore.Repository transactionRepository,
                   BalanceStore.Repository balanceRepository) {
        mPaymentWrapper = paymentWrapper;
        mBalanceRepository = balanceRepository;
        mTransactionRepository = transactionRepository;
    }

    @Override
    public void onComplete(ZPPaymentResult pPaymentResult) {
        Timber.d("pay onComplete pPaymentResult [%s]", pPaymentResult);
        boolean paymentIsCompleted = true;
        if (pPaymentResult == null) {
            if (NetworkHelper.isNetworkAvailable(mPaymentWrapper.mActivity)) {
                mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_CODE_SYSTEM);
            } else {
                mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
            }
            mPaymentWrapper.clearPendingOrder();
        } else {
            EPaymentStatus resultStatus = pPaymentResult.paymentStatus;
            Timber.d("pay onComplete resultStatus [%s]", pPaymentResult.paymentStatus);
            switch (resultStatus) {
                case ZPC_TRANXSTATUS_SUCCESS:
                    if (mPaymentWrapper.mShowNotificationLinkCard) {
                        mPaymentWrapper.mNavigator.startNotificationLinkCardActivity(mPaymentWrapper.mActivity,
                                pPaymentResult.mapCardResult);
                    }
                    mPaymentWrapper.responseListener.onResponseSuccess(pPaymentResult);
                    break;
                case ZPC_TRANXSTATUS_TOKEN_INVALID:
                    mPaymentWrapper.responseListener.onResponseTokenInvalid();
                    break;
                case ZPC_TRANXSTATUS_LOCK_USER:
                    mPaymentWrapper.responseListener.onResponseAccountSuspended();
                    break;
                case ZPC_TRANXSTATUS_UPGRADE:
                    //Hien update profile level 2
                    if (mPaymentWrapper.mRedirectListener == null) {
                        mPaymentWrapper.startUpdateProfile2ForResult();
                    } else {
                        mPaymentWrapper.mRedirectListener.startUpdateProfileLevel();
                    }

                    paymentIsCompleted = false; // will continue after update profile
                    break;
                case ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH:
                    if (mPaymentWrapper.mRedirectListener == null) {
                        mPaymentWrapper.startDepositForResult();
                    } else {
                        mPaymentWrapper.mRedirectListener.startDepositForResult();
                    }

                    paymentIsCompleted = false; // will continue after update profile
                    break;
                case ZPC_TRANXSTATUS_CLOSE:
                    mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_CODE_USER_CANCEL);
                    break;
                case ZPC_TRANXSTATUS_INPUT_INVALID:
                    mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_CODE_INPUT);
                    break;
                case ZPC_TRANXSTATUS_FAIL:
                    mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_CODE_FAIL);
                    break;
                case ZPC_TRANXSTATUS_PROCESSING:
                    mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_CODE_PROCESSING);
                    break;
                case ZPC_TRANXSTATUS_SERVICE_MAINTENANCE:
                    mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_CODE_SERVICE_MAINTENANCE);
                    break;
                case ZPC_TRANXSTATUS_NO_INTERNET:
                    mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_TRANXSTATUS_NO_INTERNET);
                    break;
                case ZPC_TRANXSTATUS_NEED_LINKCARD:
                    mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_TRANXSTATUS_NEED_LINKCARD);
                    break;
                case ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT:
                    mPaymentWrapper.responseListener.onResponseError(PaymentError.ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT);
                    break;
                case ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT_BEFORE_PAYMENT:
                    if (mPaymentWrapper.mRedirectListener == null) {
                        mPaymentWrapper.startLinkAccountActivity();
                    } else {
                        mPaymentWrapper.mRedirectListener.startLinkAccountActivity();
                    }
                    paymentIsCompleted = false; // will continue after update profile
                    break;
                case ZPC_TRANXSTATUS_UPLEVEL_AND_LINK_BANKACCOUNT_CONTINUE_PAYMENT:
                    if (mPaymentWrapper.mRedirectListener == null) {
                        mPaymentWrapper.startUpdateProfileBeforeLinkAcc();
                    } else {
                        mPaymentWrapper.mRedirectListener.startUpdateProfileBeforeLinkAcc();
                    }
                    paymentIsCompleted = false; // will continue after update profile
                    break;
                default:
                    mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_CODE_UNKNOWN);
                    break;
            }

            if (mPaymentWrapper.shouldClearPendingOrder(resultStatus)) {
                mPaymentWrapper.clearPendingOrder();
            }
        }

        // cleanup temporary variables
        if (paymentIsCompleted) {
            mPaymentWrapper.cleanup();
        }
    }

    @Override
    public void onError(CError cError) {
        Timber.d("pay onError code [%s] msg [%s]", cError.payError, cError.messError);
        switch (cError.payError) {
            case DATA_INVALID:
                mPaymentWrapper.responseListener.onParameterError(cError.messError);
                break;
            case COMPONENT_NULL:
                mPaymentWrapper.responseListener.onAppError(cError.messError);
                break;
            case NETWORKING_ERROR:
                mPaymentWrapper.responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
                break;
            default:
                mPaymentWrapper.responseListener.onAppError(cError.messError);
                break;
        }

        mPaymentWrapper.cleanup();
    }

    @Override
    public void onUpVersion(boolean forceUpdate, String latestVersion, String msg) {
        Timber.d("onUpVersion forceUpdate[%s] latestVersion [%s] msg [%s]",
                forceUpdate, latestVersion, msg);
        AppVersionUtils.handleEventUpdateVersion(mPaymentWrapper.mActivity,
                forceUpdate, latestVersion, msg);
    }

    @Override
    public void onUpdateAccessToken(String token) {
        if (!TextUtils.isEmpty(token)) {
            return;
        }
        EventBus.getDefault().post(new NewSessionEvent(token));
    }

    @Override
    public void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId) {

        mPaymentWrapper.responseListener.onPreComplete(isSuccessful, pTransId, pAppTransId);

        if (isSuccessful) {
            updateBalance();
            updateTransactionSuccess();
        } else {
            updateTransactionFail();
        }
    }

    private void updateTransactionSuccess() {
        Subscription subscription = mTransactionRepository.fetchTransactionHistorySuccessLatest()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    private void updateTransactionFail() {
        Subscription subscription = mTransactionRepository.fetchTransactionHistoryFailLatest()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    private void updateBalance() {
        // update balance
        Subscription subscription = mBalanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }
}
