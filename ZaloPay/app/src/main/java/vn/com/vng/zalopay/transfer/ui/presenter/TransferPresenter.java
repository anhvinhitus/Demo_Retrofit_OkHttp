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
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;


/**
 * Created by longlv on 13/06/2016.
 * Controller for transfer money
 */
public class TransferPresenter extends BaseUserPresenter implements TransferMoneyPresenter {

    private ITransferView mView;
    private PaymentWrapper paymentWrapper;
    private User user;

//    private ZaloFriend mCurrentZaloFriend;
//    private MappingZaloAndZaloPay mCurrentMappingZaloAndZaloPay;
//    private long mCurrentAmount;
//    private String mCurrentMessage;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private String mValidMinAmount;
    private String mValidMaxAmount;
    private RecentTransaction mTransaction;

    private void clearCurrentData() {
//        mCurrentZaloFriend = null;
//        mCurrentMappingZaloAndZaloPay = null;
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
                if (zpPaymentResult == null || zpPaymentResult.paymentInfo == null) {
                    return;
                }

                saveTransferRecentToDB();
                clearCurrentData();
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

    private void getReceiverProfile() {
        Subscription subscription = accountRepository.getUserInfoByZaloPayId(mTransaction.getZaloPayId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new PersonInfoSubscriber());
        compositeSubscription.add(subscription);
    }

    private final class PersonInfoSubscriber extends DefaultSubscriber<Person> {
        PersonInfoSubscriber() {
        }

        @Override
        public void onNext(Person item) {
            Timber.d("PersonInfoSubscriber success");
            onUpdateReceiverInfo(item);
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

            Timber.w(e, "PersonInfoSubscriber onError " + e);
            onGetUserProfileError(e);
        }
    }

    private final class GetUserInfoSubscriber extends DefaultSubscriber<MappingZaloAndZaloPay> {
        GetUserInfoSubscriber() {
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
                    mTransaction.getZaloPayId(),
                    mTransaction.message)

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
        paymentWrapper.transfer(order, mTransaction.getDisplayName(), mTransaction.getAvatar(), mTransaction.getZaloPayName());
        mView.hideLoading();
        mView.setEnableBtnContinue(true);
    }

    private void saveTransferRecentToDB() {
        try {
            if (mTransaction == null) {
                return;
            }

//            int transactionType = Integer.valueOf(ETransactionType.WALLET_TRANSFER.toString());

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

        if (TextUtils.isEmpty(mTransaction.zaloPayId)) {
            Timber.d("Empty ZaloPayID, try to convert from zaloid -> zalopayId");
            getUserMapping(mTransaction.getZaloId());
        }

        if (TextUtils.isEmpty(mTransaction.getDisplayName())) {
            Timber.d("Empty display name, try to fetch profile info from server");
            getReceiverProfile();
        }

        mValidMinAmount = String.format(mView.getContext().getString(R.string.min_money),
                CurrencyUtil.formatCurrency(Constants.MIN_TRANSFER_MONEY, true));
        mValidMaxAmount = String.format(mView.getContext().getString(R.string.max_money),
                CurrencyUtil.formatCurrency(Constants.MAX_TRANSFER_MONEY, true));

        mView.updateReceiverInfo(mTransaction.getDisplayName(),
                mTransaction.getAvatar(),
                mTransaction.getZaloPayName());

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
        mTransaction.phoneNumber = userMapZaloAndZaloPay.getPhonenumber();
        mView.updateReceiverInfo(mTransaction.getDisplayName(),
                mTransaction.getAvatar(),
                mTransaction.getZaloPayName());

        checkShowBtnContinue();
    }

    private void onUpdateReceiverInfo(Person person) {
        if (mView == null || person == null) {
            return;
        }

        mTransaction.zaloPayName = person.zalopayname;
        mTransaction.displayName = person.displayName;
        mTransaction.avatar = person.avatar;
//        mTransaction.phoneNumber = person.phonenumber;
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
        mView.getActivity().finish();
    }
}
