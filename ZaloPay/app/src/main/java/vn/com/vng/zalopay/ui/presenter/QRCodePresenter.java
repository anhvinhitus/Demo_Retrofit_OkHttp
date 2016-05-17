package vn.com.vng.zalopay.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.zing.pay.zmpsdk.ZingMobilePayService;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;

/**
 * Created by longlv on 09/05/2016.
 */

public final class QRCodePresenter extends BaseZaloPayPresenter implements Presenter<IQRScanView>, ZPPaymentListener {

    private IQRScanView mView;

    private Subscription subscriptionGetOrder;

    private User user;

    public QRCodePresenter(User user) {
        this.user = user;
    }

    @Override
    public void setView(IQRScanView view) {
        this.mView = view;
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

    public void getOrder(long appId, String zalooauthcode) {
        subscriptionGetOrder = zaloPayRepository.getOrder(appId, zalooauthcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetOrderSubscriber());
    }

    private void showOrderDetail(Order order) {
        mView.showOrderDetail(order);
    }

    private final void onGetOrderError(Throwable e) {
        hideLoadingView();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        showErrorView(message);
    }

    private final void onGetOrderSuccess(Order order) {
        Timber.d("session =========" + order.getItem());
        pay(order);
    }


    private final class GetOrderSubscriber extends DefaultSubscriber<Order> {
        public GetOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("login success " + order);
            QRCodePresenter.this.onGetOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "onError " + e);
            QRCodePresenter.this.onGetOrderError(e);
        }
    }

    //Zalo payment sdk
    private void pay(Order order) {
        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.==============");
        if (order == null) {
            showErrorView("Order not found!");
            return;
        }
        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.................2");
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (user.uid <= 0) {
            showErrorView("User info not found!");
            return;
        }
        try {
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();

            EPaymentChannel forcedPaymentChannel = null;
            paymentInfo.appID = order.getAppid();
            paymentInfo.zaloUserID = String.valueOf(user.uid);
            paymentInfo.zaloPayAccessToken = user.accesstoken;
            paymentInfo.appTime = Long.valueOf(order.getApptime());
            paymentInfo.appTransID = order.getApptransid();
            Timber.tag("_____________________").d("paymentInfo.appTransID:" + paymentInfo.appTransID);
            paymentInfo.itemName = order.getItem();
            paymentInfo.amount = Long.parseLong(order.getAmount());
            paymentInfo.description = order.getDescription();
            paymentInfo.embedData = order.getEmbeddata();
            //lap vao ví appId = appUser = 1
            paymentInfo.appUser = order.getAppuser();
            paymentInfo.mac = order.getMac();

            Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.................3");
//        paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, keyMac);
            ZingMobilePayService.pay(mView.getActivity(), forcedPaymentChannel, paymentInfo, this);
        } catch (NumberFormatException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onComplete(ZPPaymentResult pPaymentResult) {
        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("onComplete.................pPaymentResult:" + pPaymentResult);
        this.hideLoadingView();
        if (pPaymentResult == null) {
            if (!AndroidUtils.isNetworkAvailable(mView.getContext())) {
                mView.showError("Vui lòng kiểm tra kết nối mạng và thử lại.");
            } else {
                mView.showError("Lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại sau.");
            }
        } else {
            int resultStatus = pPaymentResult.paymentStatus.getNum();
            if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
                //            ToastUtil.showToast(mView.getActivity(), "Success!");
                getBalance();
                if (mView != null && mView.getActivity() != null) {
                    mView.getActivity().finish();
                }
            } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID.getNum()) {
                mView.onTokenInvalid();
            } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH.getNum()) {
                mView.getActivity().finish();
            }
        }
    }

    @Override
    public void onCancel() {
        this.hideLoadingView();
    }

    @Override
    public void onSMSCallBack(String appTransID) {

    }

    private void getBalance() {
        zaloPayRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
