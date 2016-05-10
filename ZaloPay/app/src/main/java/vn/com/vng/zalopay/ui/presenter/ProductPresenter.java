package vn.com.vng.zalopay.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.IProductDetailView;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
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
        Timber.d("session " + order.getItem());

        this.hideLoadingView();
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
//        if (order == null) {
//            showErrorView("Item not found!");
//            return;
//        }
//
//        User user = AndroidApplication.instance().getUserComponent().currentUser();
//        if (user.uid <= 0) {
//            showErrorView("User info not found!");
//            return;
//        }
//         paymentInfo = new ZPPaymentInfo();
//
//        EPaymentChannel forcedPaymentChannel = null;
//
//        paymentInfo.appID= order.getAppid();
////        paymentInfo.zaloUserID = user.uid;
//
//
////        paymentInfo.zaloPayAccessToken
//
//        paymentInfo.appTime = System.currentTimeMillis();
//
//        paymentInfo.appTransID
//        paymentInfo.items = new ArrayList<ZPPaymentItem>();
//
//        ZPPaymentItem item = new ZPPaymentItem();
//        item.itemID
//        item.itemName
//        item.itemPrice
//        item.itemQuantity
//
//        paymentInfo.items.add(item);
//        paymentInfo.amount = item.itemPrice * item.itemQuantity;
//
//        paymentInfo.description
//        paymentInfo.displayInfo
//        paymentInfo.displayName
//        paymentInfo.embedData
//
//        paymentInfo.appUser
//
//        String keyMac : will send later.
//
//                paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, keyMac);
//
//        ZingMobilePayService.pay(this, forcedPaymentChannel, paymentInfo, this);
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
