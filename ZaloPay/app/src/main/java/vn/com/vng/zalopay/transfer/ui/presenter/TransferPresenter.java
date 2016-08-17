package vn.com.vng.zalopay.transfer.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
//import vn.com.vng.zalopay.transfer.models.TransferRecent;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.transfer.ui.view.ITransferView;
import vn.com.vng.zalopay.ui.presenter.BaseZaloPayPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;

/**
 * Created by longlv on 13/06/2016.
 */
public class TransferPresenter extends BaseZaloPayPresenter implements IPresenter<ITransferView> {

    private ITransferView mView;
    private PaymentWrapper paymentWrapper;
    private User user;

    private ZaloFriend mCurrentZaloFriend;
    private MappingZaloAndZaloPay mCurrentMappingZaloAndZaloPay;
    private long mCurrentAmount;
    private String mCurrentMessage;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    TransferStore.LocalStorage mTransferLocalStorage;

    private void clearCurrentData() {
        mCurrentZaloFriend = null;
        mCurrentMappingZaloAndZaloPay = null;
    }

    public TransferPresenter(User user, TransferStore.LocalStorage localStorage) {
        this.user = user;
        this.mTransferLocalStorage = localStorage;

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
                    mView.getActivity().setResult(Activity.RESULT_OK, null);
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
        mView.onGetMappingUserError();
    }

    private void onGetMappingUserSuccess(MappingZaloAndZaloPay mappingZaloAndZaloPay) {
        mView.onGetMappingUserSucess(mappingZaloAndZaloPay);
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

    public void transferMoney(long amount, String message, ZaloFriend zaloFriend, MappingZaloAndZaloPay userMapZaloAndZaloPay) {
        if (zaloFriend == null || userMapZaloAndZaloPay == null) {
            return;
        }
        /*String phoneNumber = "";
        String appUser = "";
        String displayName = zaloFriend.getDisplayName();
        String avatar = zaloFriend.getAvatar();
        if (userMapZaloAndZaloPay != null) {
            appUser = userMapZaloAndZaloPay.getZaloPayId();
            phoneNumber = userMapZaloAndZaloPay.getPhonenumber();
        }*/
        if (user.profilelevel < 2) {
            navigator.startUpdateProfileLevel2Activity(mView.getContext(), false);
        } else {
            if (amount <= 0) {
                return;
            }
            if (user == null) {
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
            TransferRecent transferRecent = new TransferRecent(userMapZaloAndZaloPay.getZaloId(), userMapZaloAndZaloPay.getZaloPayId(), zaloFriend.getUserName(), zaloFriend.getDisplayName(), zaloFriend.getAvatar(), zaloFriend.getUserGender(), "", true, userMapZaloAndZaloPay.getPhonenumber(), transactionType, mCurrentAmount, mCurrentMessage, System.currentTimeMillis());
            mTransferLocalStorage.append(transferRecent);
        } catch (NumberFormatException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setView(ITransferView iTransferView) {
        mView = iTransferView;
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
}
