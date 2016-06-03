package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.view.IBalanceTopupView;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.mdl.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.wallet.ZingMobilePayService;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentChannel;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;

/**
 * Created by longlv on 10/05/2016.
 */
public class BalanceTopupPresenter extends BaseZaloPayPresenter implements IPresenter<IBalanceTopupView> {

    private IBalanceTopupView mView;

    private Subscription subscriptionGetOrder;

//    private User user;

    private PaymentWrapper paymentWrapper;

    public BalanceTopupPresenter(User user) {
//        this.user = user;
        paymentWrapper = new PaymentWrapper(null, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mView.getActivity();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                switch (param) {
                    case "order":
                        mView.showError(mView.getContext().getString(R.string.order_invalid));
                        break;
                    case "uid":
                        mView.showError(mView.getContext().getString(R.string.user_invalid));
                        break;
                }
            }

            @Override
            public void onResponseError(int status) {
                if (status == PaymentError.ERR_CODE_INTERNET) {
                    mView.showError("Vui lòng kiểm tra kết nối mạng và thử lại.");
                } else {
                    mView.showError("Lỗi xảy ra trong quá trình nạp tiền. Vui lòng thử lại sau.");
                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                transactionUpdate();
                mView.getActivity().finish();
            }

            @Override
            public void onResponseTokenInvalid() {
                mView.onTokenInvalid();
            }

            @Override
            public void onResponseCancel() {

            }
        });
    }

    @Override
    public void setView(IBalanceTopupView iBalanceTopupView) {
        this.mView = iBalanceTopupView;
    }

    @Override
    public void destroyView() {
        this.mView = null;
//        this.zpPaymentListener = null;
        this.paymentWrapper = null;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        this.destroyView();
        this.unsubscribe();
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(subscriptionGetOrder);
    }

    private void showLoadingView() {
        mView.showLoading();
    }

    private void hideLoadingView() {
        mView.hideLoading();
    }

    private void showErrorView(String message) {
        mView.showError(message);
    }

    private void createWalletorder(long amount) {
        subscriptionGetOrder = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, amount, ETransactionType.TOPUP.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CreateWalletOrderSubscriber());
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        public CreateWalletOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("login success " + order);
            BalanceTopupPresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "onError " + e);
            BalanceTopupPresenter.this.onCreateWalletOrderError(e);
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
//        pay(order);
        paymentWrapper.payWithOrder(order);
        hideLoadingView();
    }
//
//    //Zalo payment sdk
//    private void pay(Order order) {
//        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.==============");
//        if (order == null) {
//            showErrorView(mView.getContext().getString(R.string.order_invalid));
//            return;
//        }
//        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.................2");
//        User user = AndroidApplication.instance().getUserComponent().currentUser();
//        if (user.uid <= 0) {
//            showErrorView(mView.getContext().getString(R.string.user_invalid));
//            return;
//        }
//        try {
//            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
//
//            EPaymentChannel forcedPaymentChannel = null;
//            paymentInfo.appID = order.getAppid();
//            paymentInfo.zaloUserID = String.valueOf(user.uid);
//            paymentInfo.zaloPayAccessToken = user.accesstoken;
//            paymentInfo.appTime = Long.valueOf(order.getApptime());
//            paymentInfo.appTransID = order.getApptransid();
//            Timber.tag("_____________________").d("paymentInfo.appTransID:" + paymentInfo.appTransID);
//            paymentInfo.itemName = order.getItem();
//            paymentInfo.amount = Long.parseLong(order.getAmount());
//            paymentInfo.description = order.getDescription();
//            paymentInfo.embedData = order.getEmbeddata();
//            //lap vao ví appId = appUser = 1
//            paymentInfo.appUser = order.getAppuser();
//            paymentInfo.mac = order.getMac();
//
//            Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.................3");
////        paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, keyMac);
//            ZingMobilePayService.pay(mView.getActivity(), forcedPaymentChannel, paymentInfo, zpPaymentListener);
//        } catch (NumberFormatException e) {
//            if (BuildConfig.DEBUG) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void deposit(long amount) {
        if (amount <= 0) {
            showErrorView("Số tiền phải là bội của 10.000 VND");
            return;
        }
        createWalletorder(amount);
    }
//
//    ZPPaymentListener zpPaymentListener = new ZPPaymentListener() {
//        @Override
//        public void onComplete(ZPPaymentResult pPaymentResult) {
//            if (pPaymentResult == null) {
//                if (!AndroidUtils.isNetworkAvailable(mView.getContext())) {
//                    mView.showError("Vui lòng kiểm tra kết nối mạng và thử lại.");
//                } else {
//                    mView.showError("Lỗi xảy ra trong quá trình nạp tiền. Vui lòng thử lại sau.");
//                }
//            } else {
//                int resultStatus = pPaymentResult.paymentStatus.getNum();
//                if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
//                    transactionUpdate();
//                    mView.getActivity().finish();
//                } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID.getNum()) {
//                    mView.onTokenInvalid();
//                }
//            }
//        }
//
//        @Override
//        public void onCancel() {
//
//        }
//
//        @Override
//        public void onSMSCallBack(String appTransID) {
//
//        }
//    };

    private void transactionUpdate() {
        zaloPayRepository.transactionUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Boolean>());
    }
}
