package vn.com.vng.zalopay.transfer.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

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
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.data.exception.BodyException;
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
import vn.com.vng.zalopay.transfer.ui.view.ITransferView;
import vn.com.vng.zalopay.ui.presenter.BaseZaloPayPresenter;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;


/**
 * Created by longlv on 13/06/2016.
 * Controller for transfer money
 */
public class TransferPresenter extends BaseZaloPayPresenter implements TransferMoneyPresenter {

    private ITransferView mView;
    private PaymentWrapper paymentWrapper;
    private User user;

    private ZaloFriend mCurrentZaloFriend;
    private MappingZaloAndZaloPay mCurrentMappingZaloAndZaloPay;
    private long mCurrentAmount;
    private String mCurrentMessage;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private String mValidMinAmount;
    private String mValidMaxAmount;

    private void clearCurrentData() {
        mCurrentZaloFriend = null;
        mCurrentMappingZaloAndZaloPay = null;
    }

    public TransferPresenter(User user) {
        this.user = user;
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
                if (zpPaymentResult == null || zpPaymentResult.paymentInfo == null || mCurrentMappingZaloAndZaloPay == null) {
                    return;
                }
                if (!TextUtils.isEmpty(zpPaymentResult.paymentInfo.appUser) &&
                        zpPaymentResult.paymentInfo.appUser.equals(mCurrentMappingZaloAndZaloPay.getZaloPayId())) {
                    saveTransferRecentToDB(mCurrentZaloFriend, mCurrentMappingZaloAndZaloPay);
                    clearCurrentData();
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
        public GetUserInfoSubscriber() {
        }

        @Override
        public void onNext(MappingZaloAndZaloPay mappingZaloAndZaloPay) {
            Timber.d("GetUserInfoSubscriber success " + mappingZaloAndZaloPay);
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

            Timber.w(e, "GetUserInfoSubscriber onError " + e);
            TransferPresenter.this.onGetMappingUserError(e);
        }
    }

    private void onGetMappingUserError(Throwable e) {
        if (e != null && e instanceof BodyException) {
            if (((BodyException) e).errorCode == NetworkError.TOKEN_INVALID) {
                clearAndLogout();
                return;
            }
        }

        if (mView == null) {
            return;
        }

        mView.showErrorDialogThenClose(mView.getContext().getString(R.string.get_mapping_zalo_zalopay_error),
                mView.getContext().getString(R.string.txt_close));
    }

    public void getUserMapping(long zaloId) {
        if (zaloId <= 0) {
            return;
        }
        Subscription subscription = accountRepository.getUserInfo(zaloId, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetUserInfoSubscriber());
        compositeSubscription.add(subscription);
    }

    @Override
    public void transferMoney(long amount, String message, ZaloFriend zaloFriend, MappingZaloAndZaloPay userMapZaloAndZaloPay) {
        if (zaloFriend == null || userMapZaloAndZaloPay == null) {
            return;
        }
        if (user.profilelevel < 2) {
            navigator.startUpdateProfileLevel2Activity(mView.getContext(), false);
        } else {
            if (amount <= 0) {
                return;
            }

            mView.showLoading();
            Subscription subscription = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, amount,
                    ETransactionType.WALLET_TRANSFER.toString(),
                    userMapZaloAndZaloPay.getZaloPayId(), message)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CreateWalletOrderSubscriber(amount, message, zaloFriend, userMapZaloAndZaloPay));

            compositeSubscription.add(subscription);
        }

    }

    @Override
    public void transferMoney(long amount, String message, Person person) {
        if (user.profilelevel < 2) {
            navigator.startUpdateProfileLevel2Activity(mView.getContext(), false);
        } else {
            if (mView != null) {
                mView.showLoading();
            }

            Subscription subscription = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, amount,
                    ETransactionType.WALLET_TRANSFER.toString(),
                    person.zaloPayId, message)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CreateOrderSubscriber(person));

            compositeSubscription.add(subscription);
        }

    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        private long amount;
        private String message;
        private ZaloFriend zaloFriend;
        private MappingZaloAndZaloPay mappingZaloAndZaloPay;

        public CreateWalletOrderSubscriber(long amount, String message, ZaloFriend zaloFriend, MappingZaloAndZaloPay userMapZaloAndZaloPay) {
            this.amount = amount;
            this.message = message;
            this.zaloFriend = zaloFriend;
            this.mappingZaloAndZaloPay = userMapZaloAndZaloPay;

        }

        @Override
        public void onNext(Order order) {
            Timber.d("GetUserInfoSubscriber success " + order);
            if (zaloFriend == null || mappingZaloAndZaloPay == null) {
                return;
            }
            mCurrentAmount = amount;
            mCurrentMessage = message;
            mCurrentZaloFriend = zaloFriend;
            mCurrentMappingZaloAndZaloPay = mappingZaloAndZaloPay;
            TransferPresenter.this.onCreateWalletOrderSuccess(order, zaloFriend.getDisplayName(), zaloFriend.getAvatar(), mappingZaloAndZaloPay.getPhonenumber());
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

    private final class CreateOrderSubscriber extends DefaultSubscriber<Order> {

        Person person;

        public CreateOrderSubscriber(Person person) {
            this.person = person;
        }

        @Override
        public void onNext(Order order) {
            TransferPresenter.this.onCreateWalletOrderSuccess(order, person.displayName, person.avatar, String.valueOf(person.phonenumber));
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

    private void onCreateWalletOrderSuccess(Order order, String displayName, String avatar, String phoneNumber) {
        Timber.d("session =========" + order.getItem());
        paymentWrapper.transfer(order, displayName, avatar, phoneNumber);
        mView.hideLoading();
        mView.setEnableBtnContinue(true);
    }

    private void saveTransferRecentToDB(ZaloFriend zaloFriend, MappingZaloAndZaloPay userMapZaloAndZaloPay) {
        try {
            if (zaloFriend == null || userMapZaloAndZaloPay == null) {
                return;
            }
            int transactionType = Integer.valueOf(ETransactionType.WALLET_TRANSFER.toString());
            TransferRecent transferRecent = new TransferRecent(Long.valueOf(userMapZaloAndZaloPay.getZaloPayId()), userMapZaloAndZaloPay.getZaloPayId(), zaloFriend.getUserName(), zaloFriend.getDisplayName(), zaloFriend.getAvatar(), zaloFriend.getUserGender(), "", true, userMapZaloAndZaloPay.getPhonenumber(), transactionType, mCurrentAmount, mCurrentMessage, System.currentTimeMillis());
            transferRepository.append(transferRecent)
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
        mCurrentAmount = amount;
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
        mCurrentMessage = message;
    }

    /**
     * Invoke transfer process.
     * + First check to see if all inputs are validated
     * + Call api to create money transfer order
     * + Call SDK to initiate payment process
     */
    @Override
    public void doTransfer() {
        if (mCurrentZaloFriend == null) {
            return;
        }
        if (!isValidAmount()) {
            return;
        }
        transferMoney(mCurrentAmount, mCurrentMessage, mCurrentZaloFriend, mCurrentMappingZaloAndZaloPay);
        if (mView != null) {
            mView.setEnableBtnContinue(false);
        }
    }


    private boolean isValidMinAmount() {
        if (mCurrentAmount < Constants.MIN_TRANSFER_MONEY) {
            if (mView != null) {
                mView.toggleAmountError(mValidMinAmount);
            }
            return false;
        }
        return true;
    }

    private boolean isValidMaxAmount() {
        if (mCurrentAmount > Constants.MAX_TRANSFER_MONEY) {
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
        if (mCurrentZaloFriend != null) {
            Timber.d("onViewCreated zaloFriend.zaloPayId:%s", mCurrentZaloFriend.getUserId());
            String zalopayName = "";
            mView.updateReceiverInfo(mCurrentZaloFriend.getDisplayName(),
                    mCurrentZaloFriend.getAvatar(),
                    zalopayName);
            if (mCurrentMappingZaloAndZaloPay == null ||
                    TextUtils.isEmpty(mCurrentMappingZaloAndZaloPay.getZaloPayId())) {
                getUserMapping(mCurrentZaloFriend.getUserId());
            }
        }

        mValidMinAmount = String.format(mView.getContext().getString(R.string.min_money),
                CurrencyUtil.formatCurrency(Constants.MIN_TRANSFER_MONEY, true));
        mValidMaxAmount = String.format(mView.getContext().getString(R.string.max_money),
                CurrencyUtil.formatCurrency(Constants.MAX_TRANSFER_MONEY, true));

        initCurrentState();
        checkShowBtnContinue();
    }


    private void checkShowBtnContinue() {
        if (mView == null) {
            return;
        }

        if (mCurrentAmount <= 0) {
            mView.setEnableBtnContinue(false);
        } else {
            if (mCurrentMappingZaloAndZaloPay == null ||
                    TextUtils.isEmpty(mCurrentMappingZaloAndZaloPay.getZaloPayId())) {
                mView.setEnableBtnContinue(false);
                return;
            }
            mView.setEnableBtnContinue(true);
        }
    }

    private void initCurrentState() {
        if (mView == null) {
            Timber.w("View should not be null here");
            return;
        }
        mView.setInitialValue(mCurrentAmount, mCurrentMessage);
    }

    private void onGetMappingUserSuccess(MappingZaloAndZaloPay userMapZaloAndZaloPay) {
        if (userMapZaloAndZaloPay == null || mView == null) {
            return;
        }
        this.mCurrentMappingZaloAndZaloPay = userMapZaloAndZaloPay;
        String zalopayName = this.mCurrentMappingZaloAndZaloPay.getPhonenumber();

        mView.updateReceiverInfo(mCurrentZaloFriend.getDisplayName(),
                mCurrentZaloFriend.getAvatar(),
                zalopayName);

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

        mCurrentZaloFriend = zaloFriend;
        mCurrentMessage = message;
        if (amount != null) {
            mCurrentAmount = amount;
        }
        if (transaction != null && zaloFriend == null) {
            mCurrentZaloFriend = new ZaloFriend();
            mCurrentZaloFriend.setUserId(transaction.getUserId());
            mCurrentZaloFriend.setDisplayName(transaction.getDisplayName());
            mCurrentZaloFriend.setUserName(transaction.getZaloPayName());
            mCurrentZaloFriend.setAvatar(transaction.getAvatar());
            mCurrentZaloFriend.setUserGender(transaction.getUserGender());
            mCurrentZaloFriend.setUsingApp(transaction.isUsingApp());

            mCurrentMappingZaloAndZaloPay = new MappingZaloAndZaloPay(transaction.getUserId(), transaction.getZaloPayId(), transaction.getPhoneNumber());

            mCurrentAmount = transaction.getAmount();
            mCurrentMessage = transaction.getMessage();
        }
    }

    @Override
    public void navigateBack() {
        if (mView == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(Constants.ARG_AMOUNT, mCurrentAmount);
        intent.putExtra(Constants.ARG_MESSAGE, mCurrentMessage);
        mView.getActivity().setResult(Activity.RESULT_CANCELED, intent);
        mView.getActivity().finish();
    }
}
