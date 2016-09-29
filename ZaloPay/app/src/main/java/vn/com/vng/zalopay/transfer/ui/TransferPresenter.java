package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.JsonObject;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
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
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.utils.PhoneUtil;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;


/**
 * Created by longlv on 13/06/2016.
 * Controller for transfer money
 */
public class TransferPresenter extends BaseUserPresenter implements TransferMoneyPresenter {

    private ITransferView mView;
    private PaymentWrapper paymentWrapper;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private long mMinAmount;
    private long mMaxAmount;
    private String mValidMinAmount;
    private String mValidMaxAmount;
    private RecentTransaction mTransaction;
    private int mMoneyTransferMode;

    private User user;
    private NotificationStore.Repository mNotificationRepository;
    private final ZaloPayRepository mZaloPayRepository;
    private final AccountStore.Repository accountRepository;
    private final Navigator mNavigator;
    private final TransferStore.Repository mTransferRepository;
    private Context applicationContext;

    @Inject
    public TransferPresenter(User user, NotificationStore.Repository notificationRepository,
                             BalanceStore.Repository balanceRepository,
                             ZaloPayRepository zaloPayRepository,
                             TransactionStore.Repository transactionRepository,
                             AccountStore.Repository accountRepository,
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
        paymentWrapper = new PaymentWrapper(balanceRepository, zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mView.getActivity();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                if (mView == null) {
                    return;
                }
                if ("order".equalsIgnoreCase(param)) {
                    showError(mView.getContext().getString(R.string.order_invalid));
                } else if ("uid".equalsIgnoreCase(param)) {
                    showError(mView.getContext().getString(R.string.user_invalid));
                } else if ("token".equalsIgnoreCase(param)) {
                    showError(mView.getContext().getString(R.string.order_invalid));
                } else if (!TextUtils.isEmpty(param)) {
                    showError(param);
                }
                hideLoading();
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                if (mView == null) {
                    return;
                }
                hideLoading();
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                if (mView == null) {
                    return;
                }

                if (mView.getActivity() != null) {
                    mView.getActivity().setResult(Activity.RESULT_OK);
                    mView.getActivity().finish();
                }
                if (zpPaymentResult == null || zpPaymentResult.paymentInfo == null) {
                    return;
                }

                saveTransferRecentToDB();
            }

            @Override
            public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {
                Timber.d("Transaction is completed: [%s, %s]", isSuccessful, transId);
                if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_QR) {
                    if (isSuccessful) {
                        sendNotificationSuccess(transId);
                    } else {
                        sendNotificationFailed();
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
            public void onAppError(String msg) {
                if (mView == null) {
                    return;
                }
                if (mView.getContext() != null) {
                    showError(mView.getContext().getString(R.string.exception_generic));
                }
                hideLoading();
            }

            @Override
            public void onNotEnoughMoney() {
                if (mView == null) {
                    return;
                }
                mNavigator.startDepositActivity(mView.getContext());
            }
        });
    }

    private final class GetUserInfoSubscriber extends DefaultSubscriber<MappingZaloAndZaloPay> {
        GetUserInfoSubscriber() {
        }

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

        mTransaction.zaloPayId = userMapZaloAndZaloPay.getZaloPayId();
        mTransaction.zaloPayName = userMapZaloAndZaloPay.getZaloPayName();
        mTransaction.phoneNumber = PhoneUtil.formatPhoneNumber(userMapZaloAndZaloPay.getPhonenumber());
        mView.updateReceiverInfo(mTransaction.getDisplayName(),
                mTransaction.getAvatar(),
                mTransaction.getZaloPayName());

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
            if (!NetworkHelper.isNetworkAvailable(mView.getContext())) {
                showDialogThenClose(message, mView.getContext().getString(R.string.txt_close), SweetAlertDialog.WARNING_TYPE);
                return;
            }
        }
        showDialogThenClose(message, mView.getContext().getString(R.string.txt_close), SweetAlertDialog.ERROR_TYPE);
    }

    private void getUserMapping(long zaloId) {
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

    private void transferMoney() {
        if (user.profilelevel < 2) {
            mNavigator.startUpdateProfileLevel2Activity(mView.getContext());
        } else {
            if (mTransaction.amount <= 0) {
                return;
            }

            showLoading();

            Subscription subscription = mZaloPayRepository.createwalletorder(BuildConfig.PAYAPPID,
                    mTransaction.amount,
                    ETransactionType.WALLET_TRANSFER.toString(),
                    "1;" + mTransaction.getZaloPayId(),
                    mTransaction.message,
                    mTransaction.displayName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CreateWalletOrderSubscriber());

            compositeSubscription.add(subscription);
        }
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        CreateWalletOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("CreateWalletOrderSubscriber success " + order);
            TransferPresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
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
        hideLoading();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        showError(message);
        mView.setEnableBtnContinue(true);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.d("money transfer order: " + order.getItem());
        paymentWrapper.transfer(order, mTransaction.getDisplayName(), mTransaction.getAvatar(), mTransaction.getZaloPayName(), mTransaction.getZaloPayName());
        hideLoading();
        mView.setEnableBtnContinue(true);
    }

    private void saveTransferRecentToDB() {
        try {
            if (mTransaction == null) {
                return;
            }

            mTransferRepository.append(mTransaction,
                    Integer.valueOf(ETransactionType.WALLET_TRANSFER.toString()))
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<Boolean>());

        } catch (NumberFormatException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
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

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
    }

    /**
     * Update money amount as user input
     *
     * @param amount Money amount
     */
    @Override
    public void updateAmount(long amount) {
        mTransaction.amount = amount;
        if (mView == null) {
            return;
        }

        mView.toggleAmountError(null);
        isValidMaxAmount();
        checkShowBtnContinue();
    }

    /**
     * Update message as user input
     *
     * @param message message
     */
    @Override
    public void updateMessage(String message) {
        mTransaction.message = message;
    }

    /**
     * Invoke transfer process.
     * + First check to see if all inputs are validated
     * + Call api to create money transfer order
     * + Call SDK to initiate payment process
     */
    @Override
    public void doTransfer() {
        if (mTransaction == null) {
            return;
        }

        if (user.zaloPayId.equals(mTransaction.zaloPayId)) {
            if (mView != null) {
                showError(applicationContext.getString(R.string.exception_transfer_for_self));
            }
            return;
        }

        if (!isValidAmount()) {
            return;
        }


        transferMoney();
        if (mView != null) {
            mView.setEnableBtnContinue(false);
        }
    }


    private boolean isValidMinAmount() {
        if (mTransaction.amount < mMinAmount) {
            if (mView != null) {
                mView.toggleAmountError(mValidMinAmount);
            }
            return false;
        }
        return true;
    }

    private boolean isValidMaxAmount() {
        if (mTransaction.amount > mMaxAmount) {
            if (mView != null) {
                mView.toggleAmountError(mValidMaxAmount);
            }
            return false;
        }
        return true;
    }

    private boolean isValidAmount() {
        return isValidMinAmount() && isValidMaxAmount();
    }

    @Override
    public void onViewCreated() {
        if (mTransaction == null) {
            Timber.e("Transaction is still NULL");
            return;
        }

        if (TextUtils.isEmpty(mTransaction.zaloPayId)
                || TextUtils.isEmpty(mTransaction.zaloPayName)) {
            Timber.d("Empty ZaloPayID, try to convert from zaloid -> zalopayId");
            getUserMapping(mTransaction.getZaloId());
        }

        initLimitAmount();

        mView.setReceiverInfo(mTransaction.getDisplayName(),
                mTransaction.getAvatar(),
                mTransaction.getZaloPayName());

        if (TextUtils.isEmpty(mTransaction.getDisplayName()) || TextUtils.isEmpty(mTransaction.getAvatar())) {
            Timber.d("begin get user info");
            showLoading();
            Subscription subscription = accountRepository.getUserInfoByZaloPayId(mTransaction.zaloPayId)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new UserInfoSubscriber());
            compositeSubscription.add(subscription);
        }

        initCurrentState();
        checkShowBtnContinue();
    }

    private void initLimitAmount() {
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
        mValidMinAmount = String.format(mView.getContext().getString(R.string.min_money),
                CurrencyUtil.formatCurrency(mMinAmount, true));
        mValidMaxAmount = String.format(mView.getContext().getString(R.string.max_money),
                CurrencyUtil.formatCurrency(mMaxAmount, true));
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

    @Override
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
            mTransaction.avatar = zaloFriend.getAvatar();
            mTransaction.zaloId = zaloFriend.getUserId();
            mTransaction.displayName = zaloFriend.getDisplayName();
        }
    }

    @Override
    public void navigateBack() {
        if (mView == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(Constants.ARG_AMOUNT, mTransaction.amount);
        intent.putExtra(Constants.ARG_MESSAGE, mTransaction.message);
        mView.getActivity().setResult(Activity.RESULT_CANCELED, intent);

        if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_QR) {
            sendNotificationCancel();
        }
    }

    @Override
    public void setTransferMode(int mode) {
        mMoneyTransferMode = mode;
        if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_QR) {
            // Send notification to receiver
            sendNotificationPreTransfer();
        }
    }

    private void sendNotificationPreTransfer() {
        sendNotificationMessage(Constants.MoneyTransfer.STAGE_PRETRANSFER, 0, null);
    }

    private void sendNotificationSuccess(String transId) {
        sendNotificationMessage(Constants.MoneyTransfer.STAGE_TRANSFER_SUCCEEDED, mTransaction.amount, transId);
    }

    private void sendNotificationFailed() {
        sendNotificationMessage(Constants.MoneyTransfer.STAGE_TRANSFER_FAILED, 0, null);
    }

    private void sendNotificationCancel() {
        sendNotificationMessage(Constants.MoneyTransfer.STAGE_TRANSFER_CANCEL, 0, null);
    }

    private void sendNotificationMessage(int stage, long amount, String transId) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", Constants.QRCode.RECEIVE_MONEY);
        jsonObject.addProperty("displayname", user.displayName);
        jsonObject.addProperty("avatar", user.avatar);
        jsonObject.addProperty("mt_progress", stage);
        if (!TextUtils.isEmpty(transId)) {
            jsonObject.addProperty("transid", transId);
        }

        if (amount > 0) {
            jsonObject.addProperty("amount", mTransaction.amount);
        }

        String embeddata = jsonObject.toString();
        Timber.d("Send notification: %s", embeddata);
        embeddata = Base64.encodeToString(embeddata.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
        Subscription subscription = mNotificationRepository.sendNotification(mTransaction.zaloPayId, embeddata)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<BaseResponse>());
        compositeSubscription.add(subscription);
    }

    private class UserInfoSubscriber extends DefaultSubscriber<Person> {
        @Override
        public void onNext(Person person) {
            Timber.d("onNext displayName %s avatar %s", person.displayName, person.avatar);
            mTransaction.avatar = person.avatar;
            mTransaction.displayName = person.displayName;
            mTransaction.zaloPayName = person.zalopayname;

            mView.updateReceiverInfo(person.displayName, person.avatar, person.zalopayname);
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }
            if (mView == null) {
                return;
            }
            String message = ErrorMessageFactory.create(applicationContext, e);
            //If USER_NOT_EXIST then finish
            if (e instanceof BodyException) {
                int errorCode = ((BodyException) e).errorCode;
                if (errorCode == NetworkError.USER_NOT_EXIST ||
                        errorCode == NetworkError.RECEIVER_IS_LOCKED) {
                    showDialogThenClose(message, mView.getContext().getString(R.string.txt_close), SweetAlertDialog.ERROR_TYPE);
                    return;
                }
            }
            if (e instanceof NetworkConnectionException) {
                if (!NetworkHelper.isNetworkAvailable(mView.getContext())) {
                    showDialogThenClose(message, mView.getContext().getString(R.string.txt_close), SweetAlertDialog.WARNING_TYPE);
                    return;
                }
            }
            showError(message);
        }

        @Override
        public void onCompleted() {
            hideLoading();
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

    private void showError(String message) {
        if (mView == null) {
            return;
        }
        mView.showError(message);
    }

    private void showDialogThenClose(String error, String cancelText, int dialogType) {
        if (mView == null) {
            return;
        }
        mView.showDialogThenClose(error, cancelText, dialogType);
    }
}
