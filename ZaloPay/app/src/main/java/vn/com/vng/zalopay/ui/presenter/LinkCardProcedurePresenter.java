package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.mdl.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.view.ILinkCardProcedureView;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 12/05/2016.
 */
public class LinkCardProcedurePresenter extends BaseZaloPayPresenter implements IPresenter<ILinkCardProcedureView> {

    private ILinkCardProcedureView mView;

    private PaymentWrapper paymentWrapper;

    final User user;

    public LinkCardProcedurePresenter(User user) {
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
                updateBalance();

                ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
                if (paymentInfo == null) {
                    return;
                }
                mView.onAddCardSuccess(paymentInfo.mappedCreditCard);
            }

            @Override
            public void onResponseTokenInvalid() {
                mView.onTokenInvalid();
                clearAndLogout();
            }

            @Override
            public void onResponseCancel() {

            }
        });
    }

    @Override
    public void setView(ILinkCardProcedureView iLinkCardProduceView) {
        this.mView = iLinkCardProduceView;
    }

    @Override
    public void destroyView() {
        mView = null;
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
            mView.showLoading();
            String description = mView.getContext().getString(R.string.link_card);
            subscriptionGetOrder = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, value, ETransactionType.LINK_CARD.toString(), user.uid, description)
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
            LinkCardProcedurePresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "GetUserInfoSubscriber onError " + e);
            if (e != null && e instanceof BodyException) {
                if (((BodyException)e).errorCode == NetworkError.TOKEN_INVALID) {
                    clearAndLogout();
                    return;
                }
            }
            LinkCardProcedurePresenter.this.onCreateWalletOrderError(e);
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

    private void hideLoadingView() {
        mView.hideLoading();
    }

    private void showErrorView(String message) {
        mView.hideLoading();
        mView.showError(message);
    }
}
