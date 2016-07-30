package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
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

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public LinkCardProcedurePresenter(User user) {
        this.user = user;
        paymentWrapper = new PaymentWrapper(balanceRepository, zaloPayRepository,transactionRepository, new PaymentWrapper.IViewListener() {
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

            @Override
            public void onNotEnoughMoney() {

            }
        });
    }

    @Override
    public void setView(ILinkCardProcedureView iLinkCardProduceView) {
        this.mView = iLinkCardProduceView;
    }

    @Override
    public void destroyView() {
        hideLoadingView();
        unsubscribeIfNotNull(compositeSubscription);
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
//        super.destroy();
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
            showLoadingView();
            String description = mView.getContext().getString(R.string.link_card);
            Subscription subscription = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, value, ETransactionType.LINK_CARD.toString(), user.uid, description)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CreateWalletOrderSubscriber());
            compositeSubscription.add(subscription);
        }
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        public CreateWalletOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("CreateWalletOrderSubscriber onNext order: [%s]" + order);
            LinkCardProcedurePresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            Timber.w(e, "CreateWalletOrderSubscriber onError exception: [%s]" + e);
            LinkCardProcedurePresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        Timber.d("onCreateWalletOrderError exception: [%s]" + e);
        hideLoadingView();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        showErrorView(message);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.d("onCreateWalletOrderSuccess order: [%s]", order);
        paymentWrapper.linkCard(order);
        hideLoadingView();
    }

    private void showLoadingView() {
        if (mView == null) {
            return;
        }
        mView.showLoading();
    }

    private void hideLoadingView() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
    }

    private void showErrorView(String message) {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
        mView.showError(message);
    }
}
