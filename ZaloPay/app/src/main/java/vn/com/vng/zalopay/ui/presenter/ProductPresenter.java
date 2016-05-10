package vn.com.vng.zalopay.ui.presenter;

import java.util.ArrayList;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.IProductDetailView;
import vn.zing.pay.zmpsdk.ZingMobilePayService;
import vn.zing.pay.zmpsdk.entity.ZPPaymentItem;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;

/**
 * Created by longlv on 09/05/2016.
 */

public final class ProductPresenter  extends BaseZaloPayPresenter implements Presenter<IProductDetailView>, ZPPaymentListener {

    private IProductDetailView mView;

    private Subscription subscriptionGetOrder;

    private User user;

    public ProductPresenter(User user) {
        this.user = user;
    }

    @Override
    public void setView(IProductDetailView view) {
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

//        this.hideLoadingView();
//        this.showOrderDetail(order);
        pay(order);
    }


    private final class GetOrderSubscriber extends DefaultSubscriber<Order> {
        public GetOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("login success " + order);
            ProductPresenter.this.onGetOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "onError " + e);
            ProductPresenter.this.onGetOrderError(e);
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
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();

        EPaymentChannel forcedPaymentChannel = null;
        paymentInfo.appID= order.getAppid();
        paymentInfo.zaloUserID = String.valueOf(user.uid);
        paymentInfo.zaloPayAccessToken = user.accesstoken;
        paymentInfo.appTime = System.currentTimeMillis();
        paymentInfo.appTransID = order.getApptransid();
        paymentInfo.items = new ArrayList<ZPPaymentItem>();

        String item = order.getItem();
        String[] items = item.split(",");
//        if (items.length <= 0) {
//            showErrorView("Items not found!");
//            return;
//        }
//        for (String str: items) {
//            ZPPaymentItem paymentItem= new ZPPaymentItem();
//            paymentItem.itemID = str;
//            paymentItem.itemName = str;
//            paymentInfo.items.add(paymentItem);
//        }
        ZPPaymentItem paymentItem= new ZPPaymentItem();
        paymentItem.itemID = "111";
        paymentItem.itemName = "item123456";
        paymentItem.itemQuantity = 1;
        paymentItem.itemPrice = 1;
        paymentInfo.items.add(paymentItem);

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

    @Override
    public void onComplete(ZPPaymentResult pPaymentResult) {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onSMSCallBack(String appTransID) {

    }
}
