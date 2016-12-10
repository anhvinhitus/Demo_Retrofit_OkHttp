package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
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
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;


/**
 * Created by longlv on 13/06/2016.
 * Controller for transfer money
 */
public class TransferPresenter extends BaseUserPresenter implements IPresenter<ITransferView> {


    public static String mPreviousTransferId = null;

    private ITransferView mView;
    private PaymentWrapper paymentWrapper;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private RecentTransaction mTransaction;
    private int mMoneyTransferMode;
    private boolean mIsUserZaloPay = true;

    private User user;
    private NotificationStore.Repository mNotificationRepository;
    private final ZaloPayRepository mZaloPayRepository;
    private final AccountStore.Repository accountRepository;
    private final Navigator mNavigator;
    private final TransferStore.Repository mTransferRepository;
    private Context applicationContext;
    private final TransferNotificationHelper mTransferNotificationHelper;

    @Inject
    TransferPresenter(final User user, NotificationStore.Repository notificationRepository,
                      BalanceStore.Repository balanceRepository,
                      ZaloPayRepository zaloPayRepository,
                      TransactionStore.Repository transactionRepository,
                      final AccountStore.Repository accountRepository,
                      Navigator navigator,
                      TransferStore.Repository transferRepository,
                      Context applicationContext) {

        this.user = user;
        this.mNotificationRepository = notificationRepository;
        mZaloPayRepository = zaloPayRepository;
        this.accountRepository = accountRepository;
        mNavigator = navigator;
        this.mTransferRepository = transferRepository;
        this.applicationContext = applicationContext;

        mTransferNotificationHelper = new TransferNotificationHelper(mNotificationRepository, user);

        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new PaymentRedirectListener())
                .build();
    }

    void payPendingOrder() {
        if (paymentWrapper == null) {
            return;
        }
        if (paymentWrapper.hasPendingOrder()) {
            paymentWrapper.continuePayPendingOrder();
        }
    }

    private final class GetUserInfoSubscriber extends DefaultSubscriber<MappingZaloAndZaloPay> {
        @Override
        public void onNext(MappingZaloAndZaloPay mappingZaloAndZaloPay) {
            TransferPresenter.this.onGetMappingUserSuccess(mappingZaloAndZaloPay);
        }

        @Override
        public void onCompleted() {
            hideLoading();
        }

        @Override
        public void onError(Throwable e) {
            TransferPresenter.this.onGetMappingUserError(e);
        }
    }

    private void onGetMappingUserSuccess(MappingZaloAndZaloPay userMapZaloAndZaloPay) {
        if (userMapZaloAndZaloPay == null || mView == null) {
            return;
        }

        mTransaction.zaloPayId = userMapZaloAndZaloPay.zaloPayId;
        mTransaction.zaloPayName = userMapZaloAndZaloPay.zaloPayName;
        mTransaction.phoneNumber = PhoneUtil.formatPhoneNumber(userMapZaloAndZaloPay.phonenumber);
        mView.updateReceiverInfo(mTransaction.displayName,
                mTransaction.avatar,
                mTransaction.zaloPayName);

        checkShowBtnContinue();
    }

    private void onGetMappingUserError(Throwable e) {
        if (ResponseHelper.shouldIgnoreError(e)) {
            // simply ignore the error because it is handled from event subscribers
            return;
        }

        if (mView == null) {
            return;
        }
        String message = ErrorMessageFactory.create(applicationContext, e);
        if (e instanceof NetworkConnectionException) {
            showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.NO_INTERNET);
            return;
        }
        showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.ERROR_TYPE);
    }

    private void getUserMapping(long zaloId) {
        Timber.d("getUserMapping zaloId [%s]", zaloId);
        if (zaloId <= 0 || mView == null) {
            return;
        }
        showLoading();
        Subscription subscription = accountRepository.getUserInfo(zaloId, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetUserInfoSubscriber());
        compositeSubscription.add(subscription);
    }

    public void transferMoney() {
        if (user.profilelevel < 2) {
            mNavigator.startUpdateProfileLevel2Activity(mView.getContext());
        } else {
            if (mTransaction.amount <= 0) {
                return;
            }

            showLoading();
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

            compositeSubscription.add(subscription);
        }
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        @Override
        public void onNext(Order order) {
            Timber.d("CreateWalletOrderSubscriber success " + order);
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
        if (mView == null || mView.getContext() == null) {
            return;
        }
        if (e instanceof NetworkConnectionException) {
            mView.showNetworkErrorDialog();
        } else {
            String message = ErrorMessageFactory.create(mView.getContext(), e);
            mView.showError(message);
        }
        hideLoading();
        mView.setEnableBtnContinue(true);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.d("money transfer order: " + order.item);
        paymentWrapper.transfer(mView.getActivity(), order, mTransaction.displayName, mTransaction.avatar, mTransaction.phoneNumber, mTransaction.zaloPayName);
        hideLoading();
        mView.setEnableBtnContinue(true);
    }

    private void saveTransferRecentToDB() {
        try {
            if (mTransaction == null) {
                return;
            }

            if (TextUtils.isEmpty(mTransaction.displayName)) {
                return;
            }

            mTransferRepository.append(mTransaction,
                    Integer.valueOf(ETransactionType.WALLET_TRANSFER.toString()))
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<Boolean>());

        } catch (NumberFormatException e) {
            Timber.w(e, "saveTransferRecentToDB, cast TransactionType exception [%s]", e.getMessage());
        }
    }

    @Override
    public void setView(ITransferView view) {
        mView = view;
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        this.mView = null;
        mTransaction = null;
    }

    @Override
    public void resume() {
        checkShowBtnContinue();
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
    }

    /**
     * Update message as user input
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
        if (mTransaction == null) {
            return;
        }
        mTransaction.amount = amount;

        if (user.zaloPayId.equals(mTransaction.zaloPayId)) {
            if (mView != null) {
                mView.showError(applicationContext.getString(R.string.exception_transfer_for_self));
            }
            return;
        }

        if (mIsUserZaloPay) {
            transferMoney();
            if (mView != null) {
                mView.setEnableBtnContinue(false);
            }
        } else {
            if (mView != null) {
                mView.confirmTransferUnRegistryZaloPay();
            }
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
            getUserMapping(mTransaction.zaloId);
        }

        initLimitAmount();

        mView.setReceiverInfo(mTransaction.displayName,
                mTransaction.avatar,
                mTransaction.zaloPayName);

        if (TextUtils.isEmpty(mTransaction.displayName)
                || TextUtils.isEmpty(mTransaction.avatar)
                || TextUtils.isEmpty(mTransaction.zaloPayName)) {
            showLoading();
            getUserInfoByZaloPayId(mTransaction.zaloPayId);
        }

        initCurrentState();
        checkShowBtnContinue();
    }

    private void getUserInfoByZaloPayId(String zaloPayId) {
        if (TextUtils.isEmpty(zaloPayId)) {
            return;
        }
        Timber.d("getUserInfoByZaloPayId zaloPayId [%s]", zaloPayId);
        Subscription subscription = accountRepository.getUserInfoByZaloPayId(zaloPayId)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UserInfoSubscriber(mTransaction, mView, this));
        compositeSubscription.add(subscription);
    }

    private void initLimitAmount() {
        long mMinAmount = 0;
        long mMaxAmount = 0;
        try {
            mMinAmount = CShareData.getInstance().getMinTranferValue();
            mMaxAmount = CShareData.getInstance().getMaxTranferValue();
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
            Timber.d("Change user tranfer money");
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
            data.putExtra("code", 0);  // user cancel
            mView.getActivity().setResult(Activity.RESULT_OK, data);
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(Constants.ARG_AMOUNT, mTransaction.amount);
        intent.putExtra(Constants.ARG_MESSAGE, mTransaction.message);
        mView.getActivity().setResult(Activity.RESULT_CANCELED, intent);

        if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_QR) {

            compositeSubscription.add(mTransferNotificationHelper.sendNotificationMessage(
                    mTransaction.zaloPayId,
                    Constants.MoneyTransfer.STAGE_TRANSFER_CANCEL, 0, null
            ));
        }
    }

    public void setTransferMode(int mode) {
        mMoneyTransferMode = mode;
        if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_QR) {
            // Send notification to receiver
            compositeSubscription.add(mTransferNotificationHelper.sendNotificationMessage(
                    mTransaction.zaloPayId,
                    Constants.MoneyTransfer.STAGE_PRETRANSFER, 0, null
            ));
        }
    }

    private static class UserInfoSubscriber extends DefaultSubscriber<Person> {
        private final RecentTransaction mTransaction;
        private ITransferView mTransferView;
        private WeakReference<TransferPresenter> mPresenterWeakReference;

        UserInfoSubscriber(RecentTransaction transaction,
                           ITransferView view,
                           TransferPresenter presenter) {
            mTransaction = transaction;
            mTransferView = view;
            mPresenterWeakReference = new WeakReference<>(presenter);
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

            mTransferView.updateReceiverInfo(person.displayName, person.avatar, person.zalopayname);
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }
            if (mTransferView == null) {
                return;
            }
            if (mPresenterWeakReference.get() == null) {
                return;
            }

            String message = ErrorMessageFactory.create(mPresenterWeakReference.get().applicationContext, e);
            //If USER_NOT_EXIST then finish
            if (e instanceof BodyException) {
                int errorCode = ((BodyException) e).errorCode;
                if (errorCode == NetworkError.USER_NOT_EXIST ||
                        errorCode == NetworkError.RECEIVER_IS_LOCKED) {
                    mPresenterWeakReference.get().showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.ERROR_TYPE);
                    return;
                }
            }
            if (e instanceof NetworkConnectionException) {
                if (!NetworkHelper.isNetworkAvailable(mTransferView.getContext())) {
                    mPresenterWeakReference.get().showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.WARNING_TYPE);
                    return;
                }
            }
            mTransferView.showError(message);
        }

        @Override
        public void onCompleted() {
            if (mPresenterWeakReference.get() == null) {
                return;
            }

            mPresenterWeakReference.get().hideLoading();
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
                handleFailedTransferZalo(mView.getActivity());
            }
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (mView == null) {
                return;
            }

            if (mView.getActivity() != null) {
                if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_ZALO) {
                    handleCompletedTransferZalo(mView.getActivity());
                } else {
                    mView.getActivity().setResult(Activity.RESULT_OK);
                    mView.getActivity().finish();
                }
            }
            if (zpPaymentResult == null || zpPaymentResult.paymentInfo == null) {
                return;
            }

            saveTransferRecentToDB();
        }

        private void handleFailedTransferZalo(Activity activity) {
            Intent data = new Intent();
            data.putExtra("code", 2);
            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }

        private void handleCompletedTransferZalo(Activity activity) {
            Intent data = new Intent();
            data.putExtra("code", 1);
            data.putExtra("amount", mTransaction.amount);
            data.putExtra("message", mTransaction.message);
            data.putExtra("transactionId", mTransaction.transactionId);

            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {
            Timber.d("Transaction is completed: [%s, %s]", isSuccessful, transId);
            mTransaction.transactionId = transId;
            if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_QR) {
                if (isSuccessful) {
                    compositeSubscription.add(mTransferNotificationHelper.sendNotificationMessage(
                            mTransaction.zaloPayId,
                            Constants.MoneyTransfer.STAGE_TRANSFER_SUCCEEDED, mTransaction.amount, transId
                    ));
                } else {
                    compositeSubscription.add(mTransferNotificationHelper.sendNotificationMessage(
                            mTransaction.zaloPayId,
                            Constants.MoneyTransfer.STAGE_TRANSFER_FAILED, 0, null
                    ));
                }
            }
        }

        @Override
        public void onResponseTokenInvalid() {
            if (mView == null) {
                return;
            }
            mView.onTokenInvalid();
            clearAndLogout();
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
