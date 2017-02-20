package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.GenericException;
import vn.com.vng.zalopay.data.exception.ItemNotFoundException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;


/**
 * Created by longlv on 13/06/2016.
 * Controller for transfer money
 */
public class TransferPresenter extends AbstractPresenter<ITransferView> {


    public static String mPreviousTransferId = null;

    private PaymentWrapper paymentWrapper;

    private RecentTransaction mTransaction;
    private int mMoneyTransferMode;
    private boolean mIsUserZaloPay = true;

    private User mUser;
    private final ZaloPayRepository mZaloPayRepository;
    private final FriendStore.Repository mFriendRepository;

    private final Navigator mNavigator;
    private final TransferStore.Repository mTransferRepository;
    private Context applicationContext;
    private final TransferNotificationHelper mTransferNotificationHelper;
    private final EventBus mEventBus;
    private final ZaloSdkApi mZaloSdkApi;

    @Inject
    TransferPresenter(final User user, NotificationStore.Repository notificationRepository,
                      BalanceStore.Repository balanceRepository,
                      ZaloPayRepository zaloPayRepository,
                      TransactionStore.Repository transactionRepository,
                      Navigator navigator,
                      TransferStore.Repository transferRepository,
                      Context applicationContext, EventBus eventBus, ZaloSdkApi zaloSdkApi,
                      FriendStore.Repository friendRepository
    ) {

        this.mUser = user;
        this.mZaloPayRepository = zaloPayRepository;
        this.mNavigator = navigator;
        this.mTransferRepository = transferRepository;
        this.applicationContext = applicationContext;

        this.mEventBus = eventBus;
        mTransferNotificationHelper = new TransferNotificationHelper(notificationRepository, user);

        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new PaymentRedirectListener())
                .build();
        mZaloSdkApi = zaloSdkApi;
        this.mFriendRepository = friendRepository;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (paymentWrapper == null) {
            return;
        }

        paymentWrapper.onActivityResult(requestCode, resultCode, data);
    }

    private void getUserInfo(long zaloId) {
        Timber.d("getUserInfo zaloId [%s]", zaloId);
        if (zaloId <= 0 || mView == null) {
            return;
        }

        Subscription subscription = mFriendRepository.getUserInfo(zaloId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ZaloPayUserSubscriber());
        mSubscription.add(subscription);
    }


    private class ZaloPayUserSubscriber extends DefaultSubscriber<Person> {
        @Override
        public void onStart() {
            showLoading();
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "get zalopay user error");

            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            if (mView == null) {
                return;
            }
            hideLoading();
            String message = ErrorMessageFactory.create(applicationContext, e);

            if (e instanceof NetworkConnectionException) {
                showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.NO_INTERNET);
                return;
            }

            showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.ERROR_TYPE);
        }

        @Override
        public void onNext(Person person) {
            Timber.d("onNext displayName %s avatar %s", person.displayName, person.avatar);

            if (!TextUtils.isEmpty(person.avatar)) {
                mTransaction.avatar = person.avatar;
            }

            if (!TextUtils.isEmpty(person.displayName)) {
                mTransaction.displayName = person.displayName;
            }

            if (!TextUtils.isEmpty(person.zalopayname)) {
                mTransaction.zaloPayName = person.zalopayname;
            }

            mTransaction.zaloPayId = person.zaloPayId;
            mTransaction.zaloPayName = person.zalopayname;
            mTransaction.phoneNumber = PhoneUtil.formatPhoneNumber(person.phonenumber);

            if (mView == null) {
                return;
            }

            hideLoading();
            mView.updateReceiverInfo(person.displayName, person.avatar, person.zalopayname);
        }
    }

    public void transferMoney() {
        if (mUser.profilelevel < 2) {
            mNavigator.startUpdateProfileLevel2Activity(mView.getContext());
        } else {
            if (mTransaction.amount <= 0) {
                return;
            }

            mPreviousTransferId = null;
            Subscription subscription = mZaloPayRepository.createwalletorder(BuildConfig.ZALOPAY_APP_ID,
                    mTransaction.amount,
                    ETransactionType.WALLET_TRANSFER.toString(),
                    "1;" + mTransaction.zaloPayId,
                    mTransaction.message,
                    mTransaction.displayName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CreateWalletOrderSubscriber());

            mSubscription.add(subscription);
        }
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {

        @Override
        public void onStart() {
            showLoading();
        }

        @Override
        public void onNext(Order order) {
            Timber.d("Create order success [%s]", order);
            TransferPresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            Timber.e(e, "Server responses with error");
            TransferPresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        if (mView == null) {
            return;
        }

        hideLoading();

        if (e instanceof NetworkConnectionException) {
            mView.showNetworkErrorDialog();
        } else {
            String message = ErrorMessageFactory.create(applicationContext, e);
            mView.showError(message);
        }
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.d("money transfer order: %s", order.item);
        if (mView == null) {
            return;
        }

        paymentWrapper.transfer(mView.getActivity(), order, mTransaction.displayName, mTransaction.avatar, mTransaction.phoneNumber, mTransaction.zaloPayName);
        hideLoading();
    }

    private void saveTransferRecentToDB() {
        if (mTransaction == null || TextUtils.isEmpty(mTransaction.displayName)) {
            return;
        }

        int transactionType;

        try {
            transactionType = Integer.valueOf(ETransactionType.WALLET_TRANSFER.toString());
        } catch (NumberFormatException e) {
            Timber.d(e, "parse transaction type");
            return;
        }

        Subscription subscription = mTransferRepository.append(mTransaction, transactionType)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    @Override
    public void detachView() {
        mTransaction = null;
        super.detachView();
    }

    @Override
    public void resume() {
    }

    /**
     * Update message as mUser input
     *
     * @param message message
     */
    public void updateMessage(String message) {
        mTransaction.message = message;
    }

    /**
     * Invoke transfer process.
     * + First check to see if all inputs are validated
     * + Call api to create money transfer order
     * + Call SDK to initiate payment process
     */

    public String getZaloPayId() {
        if (mTransaction != null) {
            return mTransaction.zaloPayId;
        }
        return null;
    }

    public void doTransfer(long amount) {
        if (mView == null) {
            Timber.d("mView is null");
            return;
        }

        if (mTransaction == null) {
            Timber.d("transaction is null");
            return;
        }

        mTransaction.amount = amount;

        if (mUser.zaloPayId.equals(mTransaction.zaloPayId)) {
            mView.showError(applicationContext.getString(R.string.exception_transfer_for_self));
            return;
        }

        if (mIsUserZaloPay) {
            transferMoney();
        } else {
            mView.confirmTransferUnRegistryZaloPay();
        }
    }

    public void onViewCreated() {
        if (mTransaction == null) {
            Timber.e("Transaction is still NULL");
            return;
        }

        Timber.d("onViewCreated zaloPayId [%s] zaloPayName [%s]",
                mTransaction.zaloPayId, mTransaction.zaloPayName);
        if (TextUtils.isEmpty(mTransaction.zaloPayId)
                || TextUtils.isEmpty(mTransaction.zaloPayName)) {
            getUserInfo(mTransaction.zaloId);
        }

        initLimitAmount();

        mView.setReceiverInfo(mTransaction.displayName,
                mTransaction.avatar,
                mTransaction.zaloPayName);

        initCurrentState();
        checkShowBtnContinue();
        ensureHaveProfile();
    }

    private void ensureHaveProfile() {
        if (TextUtils.isEmpty(mUser.displayName)) {
            mZaloSdkApi.getProfile();
        }
    }

    private void initLimitAmount() {
        long mMinAmount = 0;
        long mMaxAmount = 0;
        try {
            mMinAmount = CShareDataWrapper.getMinTranferValue();
            mMaxAmount = CShareDataWrapper.getMaxTranferValue();
        } catch (Exception e) {
            Timber.w(e, "Get min/max deposit from paymentSDK exception: [%s]", e.getMessage());
        }
        if (mMinAmount <= 0) {
            mMinAmount = Constants.MIN_TRANSFER_MONEY;
        }
        if (mMaxAmount <= 0) {
            mMaxAmount = Constants.MAX_TRANSFER_MONEY;
        }

        if (mView != null) {
            mView.setMinMaxMoney(mMinAmount, mMaxAmount);
        }

    }

    private void checkShowBtnContinue() {
        if (mView == null) {
            return;
        }
        Timber.d("checkShowBtnContinue amount %s zalopayId %s", mTransaction.amount, mTransaction.zaloPayId);

        if (mTransaction.amount <= 0) {
            mView.setEnableBtnContinue(false);
            return;
        }

        if (TextUtils.isEmpty(mTransaction.zaloPayId)) {
            mView.setEnableBtnContinue(false);
            return;
        }

        mView.setEnableBtnContinue(true);
    }

    private void initCurrentState() {
        if (mView == null) {
            Timber.w("View should not be null here");
            return;
        }
        mView.setInitialValue(mTransaction.amount, mTransaction.message);
    }

    public void initView(ZaloFriend zaloFriend, RecentTransaction transaction, Long amount, String message) {
        Timber.d("initView with zaloFriend: %s, transaction: %s, amount: %s, message: %s",
                zaloFriend == null ? "null" : "NOT null",
                transaction == null ? "null" : "NOT null",
                amount == null ? "null" : amount,
                TextUtils.isEmpty(message) ? "null" : message
        );

        mTransaction = transaction;
        if (mTransaction == null) {
            mTransaction = new RecentTransaction();
            mTransaction.message = message;
            if (amount != null) {
                mTransaction.amount = amount;
            }
        }

        if (zaloFriend != null) {
            mTransaction.avatar = zaloFriend.avatar;
            mTransaction.zaloId = zaloFriend.userId;
            mTransaction.displayName = zaloFriend.displayName;
            mIsUserZaloPay = zaloFriend.usingApp;
        }

        if (mPreviousTransferId != null && !mPreviousTransferId.equals(mTransaction.zaloPayId)) {
            Timber.d("Change mUser tranfer money");
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_CHANGERECEIVER);
        }
    }

    public void navigateBack() {
        if (mView == null) {
            return;
        }
        if (mTransaction != null && mTransaction.zaloPayId != null) {
            mPreviousTransferId = mTransaction.zaloPayId;
        }

        if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_ZALO) {
            // return to Zalo
            Intent data = new Intent();
            data.putExtra("code", 0);  // mUser cancel
            mView.getActivity().setResult(Activity.RESULT_OK, data);
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(Constants.ARG_AMOUNT, mTransaction.amount);
        intent.putExtra(Constants.ARG_MESSAGE, mTransaction.message);
        mView.getActivity().setResult(Activity.RESULT_CANCELED, intent);

        if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_QR) {

            mSubscription.add(mTransferNotificationHelper.sendNotificationMessage(
                    mTransaction.zaloPayId,
                    Constants.MoneyTransfer.STAGE_TRANSFER_CANCEL, 0, null
            ));
        }
    }

    public void setTransferMode(int mode) {
        mMoneyTransferMode = mode;
        if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_QR) {
            // Send notification to receiver
            mSubscription.add(mTransferNotificationHelper.sendNotificationMessage(
                    mTransaction.zaloPayId,
                    Constants.MoneyTransfer.STAGE_PRETRANSFER, 0, null
            ));
        }
    }

    private void showLoading() {
        if (mView == null) {
            return;
        }
        mView.showLoading();
    }

    private void hideLoading() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
    }

    private void showDialogThenClose(String error, int cancelText, int dialogType) {
        if (mView == null) {
            return;
        }
        mView.showDialogThenClose(error, mView.getContext().getString(cancelText), dialogType);
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {

        @Override
        protected ILoadDataView getView() {
            return mView;
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            if (mView == null || mView.getActivity() == null) {
                return;
            }

            super.onResponseError(paymentError);

            hideLoading();

            if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_ZALO) {
                handleFailedTransferZalo(mView.getActivity(), paymentError.value());
            }
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (mView == null || mView.getActivity() == null) {
                return;
            }

            if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_ZALO) {
                handleCompletedTransferZalo(mView.getActivity());
            } else {
                mView.getActivity().setResult(Activity.RESULT_OK);
                mView.getActivity().finish();
            }

            saveTransferRecentToDB();
        }

        private void handleFailedTransferZalo(Activity activity, int code) {
            Timber.d("handleFailedTransferZalo: ");
            Intent data = new Intent();
            data.putExtra("code", code);
            activity.setResult(Activity.RESULT_OK, data);
            if (code != PaymentError.ERR_CODE_USER_CANCEL.value()) {
                activity.finish();
            }
        }

        private void handleCompletedTransferZalo(Activity activity) {
            Intent data = new Intent();
            data.putExtra("code", 1);
            data.putExtra("amount", mTransaction.amount);
            data.putExtra("message", mTransaction.message);
            data.putExtra("transactionId", mTransaction.transactionId);
            Timber.d("onResponseSuccess: isTaskRoot %s", activity.isTaskRoot());
            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {
            Timber.d("Transaction is completed: [%s, %s]", isSuccessful, transId);
            mTransaction.transactionId = transId;
            if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_QR) {
                if (isSuccessful) {
                    mSubscription.add(mTransferNotificationHelper.sendNotificationMessage(
                            mTransaction.zaloPayId,
                            Constants.MoneyTransfer.STAGE_TRANSFER_SUCCEEDED, mTransaction.amount, transId
                    ));
                } else {
                    mSubscription.add(mTransferNotificationHelper.sendNotificationMessage(
                            mTransaction.zaloPayId,
                            Constants.MoneyTransfer.STAGE_TRANSFER_FAILED, 0, null
                    ));
                }
            }
        }

        @Override
        public void onNotEnoughMoney() {
            if (mView == null) {
                return;
            }
            mNavigator.startDepositForResultActivity(mView.getFragment());
        }
    }

    private class PaymentRedirectListener implements PaymentWrapper.IRedirectListener {
        @Override
        public void startUpdateProfileLevel(String walletTransId) {
            if (mView == null || mView.getFragment() == null) {
                return;
            }
            mNavigator.startUpdateProfile2ForResult(mView.getFragment(), walletTransId);
        }
    }
}
