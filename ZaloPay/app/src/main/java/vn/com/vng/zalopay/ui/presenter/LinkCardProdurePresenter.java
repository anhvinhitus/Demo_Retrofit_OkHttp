package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.mdl.error.PaymentError;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.view.ILinkCardProduceView;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 12/05/2016.
 */
public class LinkCardProdurePresenter extends BaseZaloPayPresenter implements IPresenter<ILinkCardProduceView> {

    private ILinkCardProduceView mView;
    private Subscription subscription;
    private Subscription subscriptionGetOrder;
    private PaymentWrapper paymentWrapper;

    User user;

    @Inject
    Navigator navigator;

    public LinkCardProdurePresenter(User user) {
        this.user = user;
        paymentWrapper = new PaymentWrapper(null, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mView.getActivity();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                mView.showError(param);
            }

            @Override
            public void onResponseError(int status) {
                if (status == PaymentError.ERR_CODE_INTERNET) {
                    mView.showError("Vui lòng kiểm tra kết nối mạng và thử lại.");
                }
//                else {
//                    mView.showError("Lỗi xảy ra trong quá trình nạp tiền. Vui lòng thử lại sau.");
//                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                transactionUpdate();
                ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
                if (paymentInfo == null) {
                    return;
                }
                mView.onAddCardSuccess(paymentInfo.mappedCreditCard);
            }

            @Override
            public void onResponseTokenInvalid() {
                mView.onTokenInvalid();
                userConfig.sigoutAndCleanData(mView.getActivity());
            }

            @Override
            public void onResponseCancel() {

            }
        });
    }

    @Override
    public void setView(ILinkCardProduceView iLinkCardProduceView) {
        this.mView = iLinkCardProduceView;
    }

    @Override
    public void destroyView() {
        mView = null;
        unsubscribeIfNotNull(subscription);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        super.destroy();
        hideLoadingView();
    }

    public void addLinkCard() {
        if (user.profilelevel < 2) {
            navigator.startUpdateProfileLevel2Activity(mView.getContext(), false);
        } else {
            long value = 10000;
            if (mView.getActivity() != null) {
                try {
                    value = CShareData.getInstance(mView.getActivity()).getLinkCardValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (user == null) {
                return;
            }
            subscriptionGetOrder = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, value, ETransactionType.LINK_CARD.toString(), user.uid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CreateWalletOrderSubscriber());
        }
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        public CreateWalletOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("GetUserInfoSubscriber success " + order);
            LinkCardProdurePresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "GetUserInfoSubscriber onError " + e);
            if (e != null && e instanceof BodyException) {
                if (((BodyException)e).errorCode == NetworkError.TOKEN_INVALID) {
                    userConfig.sigoutAndCleanData(mView.getActivity());
                    return;
                }
            }
            LinkCardProdurePresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        Timber.tag("onCreateWalletOrderError").d("session =========" + e);
        hideLoadingView();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        showErrorView(message);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.tag("onCreateWalletOrderSuccess").d("session =========" + order.getItem());
        paymentWrapper.linkCard(order);
        hideLoadingView();
    }
//
//    //Zalo payment sdk
//    private void pay(Order order) {
//        Timber.tag("LinkCardProdurePresenter").d("pay.==============");
//        if (order == null) {
//            showErrorView(mView.getContext().getString(R.string.order_invalid));
//            return;
//        }
//        Timber.tag("LinkCardProdurePresenter").d("pay.................2");
//        User user = AndroidApplication.instance().getUserComponent().currentUser();
//        if (user.uid <= 0) {
//            showErrorView(mView.getContext().getString(R.string.user_invalid));
//            return;
//        }
//        try {
//            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
//            EPaymentChannel forcedPaymentChannel = EPaymentChannel.LINK_CARD;
//            paymentInfo.appID = order.getAppid();
//            paymentInfo.zaloUserID = String.valueOf(user.uid);
//            paymentInfo.zaloPayAccessToken = user.accesstoken;
//            paymentInfo.appTime = Long.valueOf(order.getApptime());
//            paymentInfo.appTransID = order.getApptransid();
//            paymentInfo.itemName = order.getItem();
//            paymentInfo.amount = Long.parseLong(order.getAmount());
//            paymentInfo.description = order.getDescription();
//            paymentInfo.embedData = order.getEmbeddata();
//            //lap vao ví appId = appUser = 1
//            paymentInfo.appUser = order.getAppuser();
//            paymentInfo.mac = order.getMac();
//
//            Timber.tag("LinkCardProdurePresenter").d("pay.................3");
//            ZingMobilePayService.pay(mView.getActivity(), forcedPaymentChannel, paymentInfo, zpPaymentListener);
//        } catch (NumberFormatException e) {
//            if (BuildConfig.DEBUG) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    ZPPaymentListener zpPaymentListener = new ZPPaymentListener() {
//        @Override
//        public void onComplete(ZPPaymentResult zpPaymentResult) {
//            hideLoadingView();
//            if (zpPaymentResult == null) {
//                if (!AndroidUtils.isNetworkAvailable(mView.getContext())) {
//                    mView.showError("Vui lòng kiểm tra kết nối mạng và thử lại.");
//                } else {
//                    mView.showError("Lỗi xảy ra trong quá trình liên kết thẻ. Vui lòng thử lại sau.");
//                }
//            } else {
//                EPaymentStatus paymentStatus = zpPaymentResult.paymentStatus;
//                if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
//                    transactionUpdate();
//                    ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
//                    if (paymentInfo == null) {
//                        return;
//                    }
//                    mView.onAddCardSuccess(paymentInfo.mappedCreditCard);
//                } else if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID.getNum()) {
//                    mView.onTokenInvalid();
//                }
//            }
//        }
//
//        @Override
//        public void onCancel() {
//            hideLoadingView();
//        }
//
//        @Override
//        public void onSMSCallBack(String s) {
//
//        }
//    };

//    private void showLoadingView() {
//        mView.showLoading();
//    }

    private void hideLoadingView() {
        mView.hideLoading();
    }

    private void showErrorView(String message) {
        mView.hideLoading();
        mView.showError(message);
    }
}
