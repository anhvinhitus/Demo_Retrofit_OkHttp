package vn.com.vng.zalopay.transfer.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.transfer.ZaloFriendsFactory;
import vn.com.vng.zalopay.transfer.models.TransferRecent;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;
import vn.com.vng.zalopay.transfer.ui.view.ITransferView;
import vn.com.vng.zalopay.ui.presenter.BaseZaloPayPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;

/**
 * Created by longlv on 13/06/2016.
 */
public class TransferPresenter extends BaseZaloPayPresenter implements IPresenter<ITransferView> {

    private ITransferView mView;
    private Subscription subscriptionGetOrder;
    private PaymentWrapper paymentWrapper;
    private User user;

    private Order mCurrentOrder;
    private ZaloFriend mCurrentZaloFriend;
    private MappingZaloAndZaloPay mCurrentMappingZaloAndZaloPay;
    private long mCurrentAmount;
    private String mCurrentMessage;

    ZaloFriendsFactory zaloFriendsFactory;

    private void clearCurrentData() {
        mCurrentOrder = null;
        mCurrentZaloFriend = null;
        mCurrentMappingZaloAndZaloPay = null;
    }

    public TransferPresenter(User user, ZaloFriendsFactory zaloFriendsFactory) {
        this.user = user;
        this.zaloFriendsFactory = zaloFriendsFactory;
        paymentWrapper = new PaymentWrapper(zaloPayRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mView.getActivity();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                if ("order".equalsIgnoreCase(param)) {
                    mView.showError(mView.getContext().getString(R.string.order_invalid));
                } else if ("uid".equalsIgnoreCase(param)) {
                    mView.showError(mView.getContext().getString(R.string.user_invalid));
                } else if ("token".equalsIgnoreCase(param)) {
                    mView.hideLoading();
                    mView.showError(mView.getContext().getString(R.string.order_invalid));
                }
            }

            @Override
            public void onResponseError(int status) {
                mView.hideLoading();
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                updateTransaction();
                updateBalance();

                if (mView != null && mView.getActivity() != null) {
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
                mView.onTokenInvalid();
                clearAndLogout();
            }

            @Override
            public void onResponseCancel() {
                mView.hideLoading();
            }

            @Override
            public void onNotEnoughMoney() {
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
        subscriptionGetOrder = accountRepository.getuserinfo(zaloId, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetUserInfoSubscriber());
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
            subscriptionGetOrder = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, amount, ETransactionType.WALLET_TRANSFER.toString(), userMapZaloAndZaloPay.getZaloPayId(), message)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CreateWalletOrderSubscriber(amount, message, zaloFriend, userMapZaloAndZaloPay));
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
            mCurrentOrder = order;
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
            int transationType = Integer.valueOf(ETransactionType.WALLET_TRANSFER.toString());
            TransferRecent transferRecent = new TransferRecent(userMapZaloAndZaloPay.getZaloId(), userMapZaloAndZaloPay.getZaloPayId(), zaloFriend.getUserName(), zaloFriend.getDisplayName(), zaloFriend.getAvatar(), zaloFriend.getUserGender(), "", true, userMapZaloAndZaloPay.getPhonenumber(), transationType, mCurrentAmount, mCurrentMessage);
            zaloFriendsFactory.insertTransferRecent(transferRecent);
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
        unsubscribeIfNotNull(subscriptionGetOrder);
    }
}
