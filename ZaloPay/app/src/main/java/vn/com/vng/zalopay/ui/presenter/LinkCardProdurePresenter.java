package vn.com.vng.zalopay.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.ILinkCardProduceView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.zing.pay.zmpsdk.ZingMobilePayService;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;

/**
 * Created by longlv on 12/05/2016.
 */
public class LinkCardProdurePresenter extends BaseUserPresenter implements Presenter<ILinkCardProduceView> {

    private ILinkCardProduceView mView;
    private Subscription subscription;

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
        zpPaymentListener = null;
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
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();

        EPaymentChannel forcedPaymentChannel = EPaymentChannel.LINK_CARD;

        if (user == null) {
            hideLoadingView();
            mView.showError("Thông tin người dùng không hợp lệ.");
            return;
        }

        paymentInfo.zaloUserID = String.valueOf(user.uid);
        paymentInfo.zaloPayAccessToken = user.accesstoken;

        Timber.tag(TAG).d("addLinkCard..............activity=====================" +  mView.getActivity());
        ZingMobilePayService.pay(mView.getActivity(), forcedPaymentChannel, paymentInfo, zpPaymentListener);
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
                    getBalance();
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

    private void getBalance() {
        zaloPayRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
