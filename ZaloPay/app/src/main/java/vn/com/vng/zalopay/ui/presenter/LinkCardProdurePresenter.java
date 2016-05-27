package vn.com.vng.zalopay.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.ILinkCardProduceView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.wallet.ZingMobilePayService;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentChannel;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;

/**
 * Created by longlv on 12/05/2016.
 */
public class LinkCardProdurePresenter extends BaseUserPresenter implements IPresenter<ILinkCardProduceView> {

    private ILinkCardProduceView mView;
    private Subscription subscription;
    private Subscription subscriptionGetOrder;

    User user;

    public LinkCardProdurePresenter(User user) {
        this.user = user;
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
        subscriptionGetOrder = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, 10000, ETransactionType.LINK_CARD.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CreateWalletOrderSubscriber());
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        public CreateWalletOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("CreateWalletOrderSubscriber success " + order);
            LinkCardProdurePresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "CreateWalletOrderSubscriber onError " + e);
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
        pay(order);
        hideLoadingView();
    }

    //Zalo payment sdk
    private void pay(Order order) {
        Timber.tag("LinkCardProdurePresenter").d("pay.==============");
        if (order == null) {
            showErrorView(mView.getContext().getString(R.string.order_invalid));
            return;
        }
        Timber.tag("LinkCardProdurePresenter").d("pay.................2");
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (user.uid <= 0) {
            showErrorView(mView.getContext().getString(R.string.user_invalid));
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
            paymentInfo.itemName = order.getItem();
            paymentInfo.amount = Long.parseLong(order.getAmount());
            paymentInfo.description = order.getDescription();
            paymentInfo.embedData = order.getEmbeddata();
            //lap vao ví appId = appUser = 1
            paymentInfo.appUser = order.getAppuser();
            paymentInfo.mac = order.getMac();

            Timber.tag("LinkCardProdurePresenter").d("pay.................3");
            ZingMobilePayService.pay(mView.getActivity(), forcedPaymentChannel, paymentInfo, zpPaymentListener);
        } catch (NumberFormatException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    ZPPaymentListener zpPaymentListener = new ZPPaymentListener() {
        @Override
        public void onComplete(ZPPaymentResult zpPaymentResult) {
            hideLoadingView();
            if (zpPaymentResult == null) {
                if (!AndroidUtils.isNetworkAvailable(mView.getContext())) {
                    mView.showError("Vui lòng kiểm tra kết nối mạng và thử lại.");
                } else {
                    mView.showError("Lỗi xảy ra trong quá trình liên kết thẻ. Vui lòng thử lại sau.");
                }
            } else {
                EPaymentStatus paymentStatus = zpPaymentResult.paymentStatus;
                if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
                    transactionUpdate();
                    ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
                    if (paymentInfo == null) {
                        return;
                    }
                    mView.onAddCardSuccess(paymentInfo.mappedCreditCard);
                } else if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID.getNum()) {
                    mView.onTokenInvalid();
                }
            }
        }

        @Override
        public void onCancel() {
            hideLoadingView();
        }

        @Override
        public void onSMSCallBack(String s) {

        }
    };

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

    private void transactionUpdate() {
        zaloPayRepository.transactionUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Boolean>());
    }
}
