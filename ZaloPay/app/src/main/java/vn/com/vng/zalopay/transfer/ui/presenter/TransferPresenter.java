package vn.com.vng.zalopay.transfer.ui.presenter;

import android.app.Activity;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.transfer.ui.view.ITransferView;
import vn.com.vng.zalopay.ui.presenter.BaseZaloPayPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.wallet.application.ZingMobilePayService;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentChannel;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 13/06/2016.
 */
public class TransferPresenter extends BaseZaloPayPresenter implements IPresenter<ITransferView> {

    private ITransferView mView;
    private Subscription subscriptionGetOrder;
    private PaymentWrapper paymentWrapper;
    private User user;

    @Inject
    Navigator navigator;

    public TransferPresenter(User user) {
        this.user = user;
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
                if (status == EPaymentStatus.ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH.getNum()) {
                    mView.getActivity().finish();
                } else {
                    mView.hideLoading();
                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                transactionUpdate();
                if (mView != null && mView.getActivity() != null) {
                    mView.getActivity().finish();
                }
            }

            @Override
            public void onResponseTokenInvalid() {
                mView.onTokenInvalid();
                userConfig.signOutAndCleanData(mView.getActivity());
            }

            @Override
            public void onResponseCancel() {
                mView.hideLoading();
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
            Timber.e(e, "GetUserInfoSubscriber onError " + e);
            if (e != null && e instanceof BodyException) {
                if (((BodyException) e).errorCode == NetworkError.TOKEN_INVALID) {
                    userConfig.signOutAndCleanData(mView.getActivity());
                    return;
                }
            }
            TransferPresenter.this.onGetMappingUserError(e);
        }
    }

    private void onGetMappingUserError(Throwable e) {
        mView.showError("Lấy thông tin tài khoản Zalo thất bại.");
    }

    private void onGetMappingUserSuccess(MappingZaloAndZaloPay mappingZaloAndZaloPay) {
        mView.updateUserPhone(mappingZaloAndZaloPay);
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

    public void transferMoney(long amount, String message, String friendZaloId, String displayName, String avatar, String phone) {
        if (user.profilelevel < 2) {
            navigator.startUpdateProfileLevel2Activity(mView.getContext(), false);
        } else {
            if (amount <= 0) {
                return;
            }
            if (user == null) {
                return;
            }
            subscriptionGetOrder = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, amount, ETransactionType.WALLET_TRANSFER.toString(), friendZaloId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CreateWalletOrderSubscriber(displayName, avatar, phone));
        }
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        private String displayName;
        private String avatar;
        private String phoneNumber;

        public CreateWalletOrderSubscriber(String displayName, String avatar, String phoneNumber) {
            this.displayName = displayName;
            this.avatar = avatar;
            this.phoneNumber = phoneNumber;
        }

        @Override
        public void onNext(Order order) {
            Timber.d("GetUserInfoSubscriber success " + order);
            TransferPresenter.this.onCreateWalletOrderSuccess(order, displayName, avatar, phoneNumber);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "GetUserInfoSubscriber onError " + e);
            if (e != null && e instanceof BodyException) {
                if (((BodyException)e).errorCode == NetworkError.TOKEN_INVALID) {
                    userConfig.signOutAndCleanData(mView.getActivity());
                    return;
                }
            }
            TransferPresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        Timber.tag("onCreateWalletOrderError").d("session =========" + e);
        mView.hideLoading();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        mView.showError(message);
    }

    private void onCreateWalletOrderSuccess(Order order, String displayName, String avatar, String phoneNumber) {
        Timber.tag("onCreateWalletOrderSuccess").d("session =========" + order.getItem());
        paymentWrapper.transfer(order, displayName, avatar, phoneNumber);
        mView.hideLoading();
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
        super.destroy();
    }
}
