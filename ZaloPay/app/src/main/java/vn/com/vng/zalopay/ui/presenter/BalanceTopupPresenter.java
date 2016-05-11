package vn.com.vng.zalopay.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.balancetopup.ui.view.IBalanceTopupView;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.zing.pay.zmpsdk.ZingMobilePayService;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;

/**
 * Created by longlv on 10/05/2016.
 */
public class BalanceTopupPresenter extends BaseZaloPayPresenter implements Presenter<IBalanceTopupView>, ZPPaymentListener {

    private IBalanceTopupView mView;

    private Subscription subscriptionGetOrder;

    private User user;

    public BalanceTopupPresenter(User user) {
        this.user = user;
    }

    @Override
    public void setView(IBalanceTopupView iBalanceTopupView) {
        this.mView = iBalanceTopupView;
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

    private void createWalletorder(long amount) {
        subscriptionGetOrder = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, amount, 2)
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
        pay(order);
        hideLoadingView();
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
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();

        EPaymentChannel forcedPaymentChannel = null;
        paymentInfo.appID= order.getAppid();
        paymentInfo.zaloUserID = String.valueOf(user.uid);
        paymentInfo.zaloPayAccessToken = user.accesstoken;
        paymentInfo.appTime = System.currentTimeMillis();
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
    }


    public void deposit(long amount) {
        if (amount <= 0) {
            showErrorView("Số tiền phải là bội của 10.000 VNĐ");
            return;
        }
        createWalletorder(amount);
    }

    @Override
    public void onComplete(ZPPaymentResult pPaymentResult) {
        if (pPaymentResult == null) {
            return;
        }
        int resultStatus = pPaymentResult.paymentStatus.getNum();
        if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
            ToastUtil.showToast(mView.getActivity(), "Success!");
        } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_PROCESSING.getNum()) {
            ToastUtil.showToast(mView.getActivity(), "Processing!");
        } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_FAIL.getNum()) {
            ToastUtil.showToast(mView.getActivity(), "Fail!");
        }
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onSMSCallBack(String appTransID) {

    }
}
