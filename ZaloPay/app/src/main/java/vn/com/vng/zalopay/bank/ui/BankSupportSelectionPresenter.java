package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAction;
import vn.com.vng.zalopay.bank.models.BankInfo;
import vn.com.vng.zalopay.bank.models.LinkBankType;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.pw.PaymentWrapperBuilder;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by Duke on 5/25/17.
 * List support bank.
 */
public class BankSupportSelectionPresenter extends AbstractBankPresenter<IBankSupportSelectionView> {
    private PaymentWrapper paymentWrapper;
    private final User mUser;
    private LinkBankType mBankType;
    private DefaultSubscriber<List<ZPCard>> mGetSupportBankSubscriber;

    @Inject
    BankSupportSelectionPresenter(User user) {
        this.mUser = user;
        mGetSupportBankSubscriber = new DefaultSubscriber<List<ZPCard>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.d("Get support bank type [%s] onError [%s]", mBankType, e.getMessage());
                showRetryDialog(e.getMessage());
            }

            @Override
            public void onNext(List<ZPCard> cardList) {
                Timber.d("Get support bank type [%s] onComplete list card [%s]", mBankType, cardList);
                fetchListBank(cardList);
            }
        };

        paymentWrapper = new PaymentWrapperBuilder()
                .setResponseListener(new PaymentResponseListener())
                .setLinkCardListener(new LinkCardListener(this))
                .build();
        paymentWrapper.initializeComponents();
    }

    @Override
    public void attachView(IBankSupportSelectionView iBankSupportSelectionView) {
        super.attachView(iBankSupportSelectionView);
    }

    @Override
    public void destroy() {
        paymentWrapper = null;
        super.destroy();
    }

    @Override
    Activity getActivity() {
        if (mView == null)
            return null;
        return mView.getActivity();
    }

    @Override
    User getUser() {
        return mUser;
    }

    @Override
    PaymentWrapper getPaymentWrapper() {
        return paymentWrapper;
    }

    private Context getContext() {
        if (mView == null)
            return null;
        return mView.getContext();
    }

    private void showRetryDialog(String message) {
        if (mView == null || getContext() == null) {
            return;
        }

        mView.showRetryDialog(message, new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                getActivity().finish();
            }

            @Override
            public void onOKevent() {
                getCardSupport();
            }
        });
    }

    private void fetchListBank(List<ZPCard> listBank) {
        if (mView != null) {
            mView.fetchListBank(listBank);
        }
    }

    void getCardSupport() {
        Timber.d("Get list bank support %s", mBankType);
        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = mUser.zaloPayId;
        userInfo.accessToken = mUser.accesstoken;
        Subscription subscription = CShareDataWrapper.getCardSupportList(userInfo, mGetSupportBankSubscriber);
        mSubscription.add(subscription);
    }

    void initData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mBankType = (LinkBankType) bundle.getSerializable(Constants.ARG_LINK_BANK_TYPE);
    }

    void linkCard() {
        getPaymentWrapper().linkCard(getActivity());
//        setResultLinkCard();
    }

    private void setResultActivity(BankAction bankAction, DBaseMap bankInfo) {
        if (mView == null || bankInfo == null) {
            return;
        }

        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        bundle.putParcelable(Constants.BANK_DATA_RESULT_AFTER_LINK,
                new BankInfo(bankAction, bankInfo.bankcode, bankInfo.getFirstNumber(), bankInfo.getLastNumber()));
        intent.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private void setResultErrorActivity(String message) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        bundle.putString(Constants.BANK_DATA_RESULT_AFTER_LINK, message);
        intent.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_CANCELED, intent);
        getActivity().finish();
    }

    private void setResultLinkCard() {
        getActivity().setResult(10000);
        getActivity().finish();
    }

    @Override
    void onAddBankCardSuccess(DMappedCard bankCard) {
        setResultActivity(BankAction.LINK_CARD, bankCard);
    }

    @Override
    void onAddBankAccountSuccess(DBankAccount bankAccount) {
        setResultActivity(BankAction.LINK_ACCOUNT, bankAccount);
    }

    @Override
    void onUnLinkBankAccountSuccess(DBankAccount bankAccount) {
        setResultActivity(BankAction.UNLINK_ACCOUNT, bankAccount);
    }


    // Inner class custom listener
    private class PaymentResponseListener implements PaymentWrapper.IResponseListener {
        @Override
        public void onParameterError(String param) {
            setResultErrorActivity(param);
        }

        @Override
        public void onResponseError(PaymentError status) {
//            if (status == PaymentError.ERR_CODE_INTERNET) {
//                if (mView == null) {
//                    return;
//                }
//                mView.hideLoading();
//                mView.showNetworkErrorDialog();
//            }
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            onResponseSuccessFromSDK(zpPaymentResult);
        }

        @Override
        public void onResponseTokenInvalid() {
            Timber.e("Invalid token");
        }

        @Override
        public void onResponseAccountSuspended() {
            Timber.e("Account suspended");
        }

        @Override
        public void onAppError(String msg) {
            setResultErrorActivity(msg);
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId) {

        }
    }

    private class LinkCardListener implements PaymentWrapper.ILinkCardListener {
        WeakReference<BankSupportSelectionPresenter> mWeakReference;

        LinkCardListener(BankSupportSelectionPresenter presenter) {
            mWeakReference = new WeakReference<>(presenter);
        }

        @Override
        public void onErrorLinkCardButInputBankAccount(DBaseMap bankInfo) {
            if (mWeakReference.get() == null) {
                return;
            }

            mWeakReference.get().onErrorLinkCardButInputBankAccount(bankInfo);
        }
    }

    protected void onErrorLinkCardButInputBankAccount(DBaseMap bankInfo) {
    }
}