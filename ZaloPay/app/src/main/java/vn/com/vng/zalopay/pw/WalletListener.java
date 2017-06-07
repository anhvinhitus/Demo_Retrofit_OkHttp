package vn.com.vng.zalopay.pw;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.eventbus.NewSessionEvent;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.error.CError;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;

import static vn.com.zalopay.wallet.constants.PaymentError.COMPONENT_NULL;
import static vn.com.zalopay.wallet.constants.PaymentError.DATA_INVALID;
import static vn.com.zalopay.wallet.constants.PaymentError.NETWORKING_ERROR;
import static vn.com.zalopay.wallet.constants.PaymentStatus.USER_CLOSE;
import static vn.com.zalopay.wallet.constants.PaymentStatus.FAILURE;
import static vn.com.zalopay.wallet.constants.PaymentStatus.INVALID_DATA;
import static vn.com.zalopay.wallet.constants.PaymentStatus.USER_LOCK;
import static vn.com.zalopay.wallet.constants.PaymentStatus.MONEY_NOT_ENOUGH;
import static vn.com.zalopay.wallet.constants.PaymentStatus.DIRECT_LINKCARD;
import static vn.com.zalopay.wallet.constants.PaymentStatus.DIRECT_LINKCARD_AND_PAYMENT;
import static vn.com.zalopay.wallet.constants.PaymentStatus.DIRECT_LINK_ACCOUNT;
import static vn.com.zalopay.wallet.constants.PaymentStatus.DIRECT_LINK_ACCOUNT_AND_PAYMENT;
import static vn.com.zalopay.wallet.constants.PaymentStatus.DISCONNECT;
import static vn.com.zalopay.wallet.constants.PaymentStatus.PROCESSING;
import static vn.com.zalopay.wallet.constants.PaymentStatus.SERVICE_MAINTENANCE;
import static vn.com.zalopay.wallet.constants.PaymentStatus.SUCCESS;
import static vn.com.zalopay.wallet.constants.PaymentStatus.TOKEN_EXPIRE;
import static vn.com.zalopay.wallet.constants.PaymentStatus.LEVEL_UPGRADE_PASSWORD;
import static vn.com.zalopay.wallet.constants.PaymentStatus.UPLEVEL_AND_LINK_BANKACCOUNT_AND_PAYMENT;

/**
 * Created by huuhoa on 12/8/16.
 * Listener for callback from SDK
 */
class WalletListener implements ZPPaymentListener {
    private PaymentWrapper mPaymentWrapper;
    private final TransactionStore.Repository mTransactionRepository;
    private final BalanceStore.Repository mBalanceRepository;
    private CompositeSubscription mCompositeSubscription;

    WalletListener(PaymentWrapper paymentWrapper,
                   TransactionStore.Repository transactionRepository,
                   BalanceStore.Repository balanceRepository,
                   CompositeSubscription compositeSubscription) {
        mPaymentWrapper = paymentWrapper;
        mBalanceRepository = balanceRepository;
        mTransactionRepository = transactionRepository;
        mCompositeSubscription = compositeSubscription;
    }

    @Override
    public void onComplete() {
        int paymentStatus = mPaymentWrapper.getPaymentInfoBuilder().getStatus();
        DBaseMap mapBank = mPaymentWrapper.getPaymentInfoBuilder().getMapBank();
        Timber.d("pay complete, result [%d]", paymentStatus);
        boolean paymentIsCompleted = true;
        PaymentWrapper.IResponseListener responseListener = mPaymentWrapper.getResponseListener();
        if (paymentStatus == PaymentStatus.PROCESSING) {
            if (NetworkHelper.isNetworkAvailable(mPaymentWrapper.mActivity)) {
                responseListener.onResponseError(PaymentError.ERR_CODE_SYSTEM);
            } else {
                responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
            }
            mPaymentWrapper.clearPendingOrder();
        } else {
            Timber.d("pay complete, status [%s]", paymentStatus);
            switch (paymentStatus) {
                case SUCCESS:
                    if (mPaymentWrapper.mShowNotificationLinkCard) {
                        DMapCardResult mapCard = mPaymentWrapper.getPaymentInfoBuilder().getMapCard();
                        mPaymentWrapper.mNavigator.startNotificationLinkCardActivity(mPaymentWrapper.mActivity, mapCard);
                    }
                    responseListener.onResponseSuccess(mPaymentWrapper.getPaymentInfoBuilder());
                    break;
                case TOKEN_EXPIRE:
                    responseListener.onResponseTokenInvalid();
                    break;
                case USER_LOCK:
                    responseListener.onResponseAccountSuspended();
                    break;
                case LEVEL_UPGRADE_PASSWORD:
                    //Hien update profile level 2
                    if (mPaymentWrapper.mRedirectListener == null) {
                        mPaymentWrapper.startUpdateProfile2ForResult();
                    } else {
                        mPaymentWrapper.mRedirectListener.startUpdateProfileLevel();
                    }

                    paymentIsCompleted = false; // will continue after update profile
                    break;
                case MONEY_NOT_ENOUGH:
                    if (mPaymentWrapper.mRedirectListener == null) {
                        mPaymentWrapper.startDepositForResult();
                    } else {
                        mPaymentWrapper.mRedirectListener.startDepositForResult();
                    }

                    paymentIsCompleted = false; // will continue after update profile
                    break;
                case USER_CLOSE:
                    responseListener.onResponseError(PaymentError.ERR_CODE_USER_CANCEL);
                    /*// TODO: 5/29/17 - longlv: Fake data to test
                    pPaymentResult.paymentStatus = EPaymentStatus.SUCCESS;
                    //LinkAccount
                    pPaymentResult.paymentInfo.linkAccInfo = new LinkAccInfo(ECardType.PVCB.toString(), ELinkAccType.LINK);
                    DBankAccount dBankAccount = new DBankAccount();
                    dBankAccount.bankcode = ECardType.PVCB.toString();
                    dBankAccount.firstaccountno = "097654";
                    dBankAccount.lastaccountno = "4321";
                    pPaymentResult.paymentInfo.mapBank = dBankAccount;
                    //LinkCard
                    *//*DMappedCard mappedCard = new DMappedCard();
                    mappedCard.bankcode = ECardType.PBIDV.toString();
                    mappedCard.first6cardno = "970418";
                    mappedCard.last4cardno = "4321";
                    pPaymentResult.paymentInfo.mapBank = mappedCard;*//*

                    mPaymentWrapper.responseListener.onResponseSuccess(pPaymentResult);*/
                    break;
                case INVALID_DATA:
                    responseListener.onResponseError(PaymentError.ERR_CODE_INPUT);
                    break;
                case FAILURE:
                    responseListener.onResponseError(PaymentError.ERR_CODE_FAIL);
                    break;
                case PROCESSING:
                    responseListener.onResponseError(PaymentError.ERR_CODE_PROCESSING);
                    break;
                case SERVICE_MAINTENANCE:
                    responseListener.onResponseError(PaymentError.ERR_CODE_SERVICE_MAINTENANCE);
                    break;
                case DISCONNECT:
                    responseListener.onResponseError(PaymentError.ERR_TRANXSTATUS_NO_INTERNET);
                    break;
                case DIRECT_LINKCARD:
                    responseListener.onResponseError(PaymentError.ERR_TRANXSTATUS_NEED_LINKCARD);
                    break;
                case DIRECT_LINK_ACCOUNT:
                    if (mPaymentWrapper.mLinkCardListener != null) {
                        Timber.d("pay complete, switch to link account because link card but user input bank account");
                        mPaymentWrapper.mLinkCardListener.onErrorLinkCardButInputBankAccount(mapBank);
                    } else {
                        Timber.w("pay complete, response error: DIRECT_LINK_ACCOUNT");
                        responseListener.onResponseError(PaymentError.ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT);
                    }
                    break;
                case DIRECT_LINK_ACCOUNT_AND_PAYMENT:
                    String bankCode = null;
                    if (mapBank != null) {
                        bankCode = mapBank.bankcode;
                    }
                    if (mPaymentWrapper.mRedirectListener == null) {
                        mPaymentWrapper.startLinkAccountActivity(bankCode);
                    } else {
                        mPaymentWrapper.mRedirectListener.startLinkAccountActivity(bankCode);
                    }
                    paymentIsCompleted = false; // will continue after update profile
                    break;
                case DIRECT_LINKCARD_AND_PAYMENT:
                    String bankCodeLinkCard = null;
                    if (mapBank != null) {
                        bankCodeLinkCard = mapBank.bankcode;
                    }
                    if (mPaymentWrapper.mRedirectListener == null) {
                        mPaymentWrapper.startLinkCardActivity(bankCodeLinkCard);
                    } else {
                        mPaymentWrapper.mRedirectListener.startLinkCardActivity(bankCodeLinkCard);
                    }
                    paymentIsCompleted = false; // will continue after update profile
                    break;
                case UPLEVEL_AND_LINK_BANKACCOUNT_AND_PAYMENT:
                    if (mPaymentWrapper.mRedirectListener == null) {
                        mPaymentWrapper.startUpdateProfileBeforeLinkAcc();
                    } else {
                        mPaymentWrapper.mRedirectListener.startUpdateProfileBeforeLinkAcc();
                    }
                    paymentIsCompleted = false; // will continue after update profile
                    break;
                default:
                    responseListener.onResponseError(PaymentError.ERR_CODE_UNKNOWN);
                    break;
            }

            if (mPaymentWrapper.shouldClearPendingOrder(paymentStatus)) {
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

        PaymentWrapper.IResponseListener responseListener = mPaymentWrapper.getResponseListener();

        switch (cError.payError) {
            case DATA_INVALID:
                responseListener.onParameterError(cError.messError);
                break;
            case COMPONENT_NULL:
                responseListener.onAppError(cError.messError);
                break;
            case NETWORKING_ERROR:
                responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
                break;
            default:
                responseListener.onAppError(cError.messError);
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

        PaymentWrapper.IResponseListener responseListener = mPaymentWrapper.getResponseListener();
        responseListener.onPreComplete(isSuccessful, pTransId, pAppTransId);

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
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    private void updateTransactionFail() {
        Subscription subscription = mTransactionRepository.fetchTransactionHistoryFailLatest()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    private void updateBalance() {
        Subscription subscription = mBalanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }
}
