package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
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
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.utils.PhoneUtil;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;


/**
 * Created by longlv on 13/06/2016.
 * Controller for transfer money
 */
public class TransferPresenter extends BaseUserPresenter implements TransferMoneyPresenter {

    private ITransferView mView;
    private PaymentWrapper paymentWrapper;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private String mValidMinAmount;
    private String mValidMaxAmount;
    private RecentTransaction mTransaction;
    private int mMoneyTransferMode;

    private User user;
    private NotificationStore.Repository mNotificationRepository;

    @Inject
    public TransferPresenter(User user, NotificationStore.Repository notificationRepository) {
        this.user = user;
        this.mNotificationRepository = notificationRepository;
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
                    mView.showError(mView.getContext().getString(R.string.order_invalid));
                } else if ("uid".equalsIgnoreCase(param)) {
                    mView.showError(mView.getContext().getString(R.string.user_invalid));
                } else if ("token".equalsIgnoreCase(param)) {
                    mView.showError(mView.getContext().getString(R.string.order_invalid));
                } else if (!TextUtils.isEmpty(param)) {
                    mView.showError(param);
                }
                mView.hideLoading();
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                if (mView == null) {
                    return;
                }
                mView.hideLoading();
//                if (mMoneyTransferMode == Constants.MoneyTransfer.MODE_QR) {
//                    if (paymentError == PaymentError.ERR_CODE_USER_CANCEL) {
//                        sendNotificationCancel();
//                    }
                    /*else if (paymentError != PaymentError.ERR_CODE_SUCCESS) {
                          sendNotificationFailed();
                    }*/
//                }
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
                    mView.showError(mView.getContext().getString(R.string.exception_generic));
                }
                mView.hideLoading();
            }

            @Override
            public void onNotEnoughMoney() {
                if (mView == null) {
                    return;
                }
                navigator.startDepositActivity(mView.getContext());
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
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            TransferPresenter.this.onGetMappingUserError(e);
        }
    }

    private void onGetMappingUserError(Throwable e) {

        if (mView == null) {
            return;
        }

        mView.showErrorDialogThenClose(mView.getContext().getString(R.string.get_mapping_zalo_zalopay_error),
                mView.getContext().getString(R.string.txt_close));
    }

    private void onGetUserProfileError(Throwable e) {
        if (e != null && e instanceof BodyException) {
            if (((BodyException) e).errorCode == NetworkError.TOKEN_INVALID) {
                clearAndLogout();
                return;
            }
        }

        if (mView == null) {
            return;
        }

        mView.showErrorDialogThenClose(mView.getContext().getString(R.string.get_userinfo_zalopayid_error),
                mView.getContext().getString(R.string.txt_close));
    }

    private void getUserMapping(long zaloId) {
        if (zaloId <= 0) {
            return;
        }

        Subscription subscription = accountRepository.getUserInfo(zaloId, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetUserInfoSubscriber());
        compositeSubscription.add(subscription);
    }

    private void transferMoney() {
        if (user.profilelevel < 2) {
            navigator.startUpdateProfileLevel2Activity(mView.getContext(), false);
        } else {
            if (mTransaction.amount <= 0) {
                return;
            }

            mView.showLoading();

            Subscription subscription = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID,
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
        mView.hideLoading();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        mView.showError(message);
        mView.setEnableBtnContinue(true);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.d("money transfer order: " + order.getItem());
        paymentWrapper.transfer(order, mTransaction.getDisplayName(), mTransaction.getAvatar(), mTransaction.getZaloPayName(), mTransaction.getZaloPayName());
        mView.hideLoading();
        mView.setEnableBtnContinue(true);
    }

    private void saveTransferRecentToDB() {
        try {
            if (mTransaction == null) {
                return;
            }

            transferRepository.append(mTransaction,
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
        if (!isValidAmount()) {
            return;
        }
        transferMoney();
        if (mView != null) {
            mView.setEnableBtnContinue(false);
        }
    }


    private boolean isValidMinAmount() {
        if (mTransaction.amount < Constants.MIN_TRANSFER_MONEY) {
            if (mView != null) {
                mView.toggleAmountError(mValidMinAmount);
            }
            return false;
        }
        return true;
    }

    private boolean isValidMaxAmount() {
        if (mTransaction.amount > Constants.MAX_TRANSFER_MONEY) {
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
                || (TextUtils.isEmpty(mTransaction.zaloPayName)
                    && TextUtils.isEmpty(mTransaction.phoneNumber))) {
            Timber.d("Empty ZaloPayID, try to convert from zaloid -> zalopayId");
            getUserMapping(mTransaction.getZaloId());
        }

        mValidMinAmount = String.format(mView.getContext().getString(R.string.min_money),
                CurrencyUtil.formatCurrency(Constants.MIN_TRANSFER_MONEY, true));
        mValidMaxAmount = String.format(mView.getContext().getString(R.string.max_money),
                CurrencyUtil.formatCurrency(Constants.MAX_TRANSFER_MONEY, true));

        mView.updateReceiverInfo(mTransaction.getDisplayName(),
                mTransaction.getAvatar(),
                mTransaction.getZaloPayName(),
                mTransaction.getPhoneNumber());

        if (TextUtils.isEmpty(mTransaction.getDisplayName()) || TextUtils.isEmpty(mTransaction.getAvatar())) {


            Timber.d("begin get user info");

            Subscription subscription = accountRepository.getUserInfoByZaloPayId(mTransaction.zaloPayId)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new UserInfoSubscriber());
            compositeSubscription.add(subscription);
        }

        initCurrentState();
        checkShowBtnContinue();
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

    private void onGetMappingUserSuccess(MappingZaloAndZaloPay userMapZaloAndZaloPay) {
        if (userMapZaloAndZaloPay == null || mView == null) {
            return;
        }

        mTransaction.zaloPayId = userMapZaloAndZaloPay.getZaloPayId();
        mTransaction.phoneNumber = PhoneUtil.formatPhoneNumber(userMapZaloAndZaloPay.getPhonenumber());
        mView.updateReceiverInfo(mTransaction.phoneNumber);

        checkShowBtnContinue();
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

            mView.updateReceiverInfo(person.displayName, person.avatar, person.zalopayname, mTransaction.phoneNumber);
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            String message = ErrorMessageFactory.create(applicationContext, e);
            mView.showError(message);
        }
    }
}
