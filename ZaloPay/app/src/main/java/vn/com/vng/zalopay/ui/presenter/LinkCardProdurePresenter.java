package vn.com.vng.zalopay.ui.presenter;

import javax.inject.Inject;

import rx.Subscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.ILinkCardProduceView;
import vn.zing.pay.zmpsdk.ZingMobilePayService;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;

/**
 * Created by longlv on 12/05/2016.
 */
public class LinkCardProdurePresenter extends BaseUserPresenter implements Presenter<ILinkCardProduceView>, ZPPaymentListener {

    private ILinkCardProduceView mView;
    private Subscription subscription;
    private Subscription subscriptionGetOrder;

    @Inject
    UserConfig userConfig;

    public LinkCardProdurePresenter(UserConfig userConfig) {
        this.userConfig = userConfig;
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
        hideLoadingView();
    }

    public void addLinkCard() {
        showLoadingView();
//        subscriptionGetOrder = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, 100000, 2)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new CreateWalletOrderSubscriber());
        try {
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();

            EPaymentChannel forcedPaymentChannel = EPaymentChannel.LINK_CARD;
            paymentInfo.appID = BuildConfig.PAYAPPID;
            paymentInfo.zaloUserID = String.valueOf(userConfig.getUserId());
            paymentInfo.zaloPayAccessToken = userConfig.getCurrentUser().accesstoken;
//            //lap vao ví appId = appUser = 1
            paymentInfo.appUser = String.valueOf(BuildConfig.PAYAPPID);

            Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.................3");
            ZingMobilePayService.pay(mView.getActivity(), forcedPaymentChannel, paymentInfo, this);
        } catch (NumberFormatException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
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

            EPaymentChannel forcedPaymentChannel = EPaymentChannel.LINK_CARD;
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
    public void onComplete(ZPPaymentResult zpPaymentResult) {
        hideLoadingView();
        if (zpPaymentResult == null) {
            return;
        }
        EPaymentStatus paymentStatus = zpPaymentResult.paymentStatus;
        if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
            ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
            if (paymentInfo==null) {
                return;
            }
            mView.onAddCardSuccess(paymentInfo.mappedCreditCard);
        } else if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_PROCESSING.getNum()) {
            mView.showError("Giao dịch đang xử lý.");
        } else if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_FAIL.getNum()) {
            mView.showError("Giao dịch thất bại.");
        }
    }

    @Override
    public void onCancel() {
        hideLoadingView();
    }

    @Override
    public void onSMSCallBack(String s) {

    }

    private void showLoadingView() {
        mView.showLoading();
    }

    private void hideLoadingView() {
        mView.hideLoading();
    }

    private void showErrorView(String message) {
        mView.hideLoading();
        mView.showError(message);
    }

    private void onCreateWalletOrderError(Throwable e) {
        Timber.tag("LinkCardProdurePresenter").d("onCreateWalletOrderError session =========" + e);
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        showErrorView(message);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.tag("LinkCardProdurePresenter").d("onCreateWalletOrderSuccess session =========" + order.getItem());
        pay(order);
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        public CreateWalletOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.tag("LinkCardProdurePresenter").d("login success " + order);
            LinkCardProdurePresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.tag("LinkCardProdurePresenter").e(e, "onError " + e);
            LinkCardProdurePresenter.this.onCreateWalletOrderError(e);
        }
    }
}
