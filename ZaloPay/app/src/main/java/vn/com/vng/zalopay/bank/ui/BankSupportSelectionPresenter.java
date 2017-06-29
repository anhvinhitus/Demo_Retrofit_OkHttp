package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAction;
import vn.com.vng.zalopay.bank.models.BankInfo;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.merchant.entities.ZPBank;

/**
 * Created by Duke on 5/25/17.
 * List support bank.
 */
public class BankSupportSelectionPresenter extends AbstractBankPresenter<IBankSupportSelectionView> {
    private final User mUser;
    private final Context applicationContext;
    private DefaultSubscriber<List<ZPBank>> mGetSupportBankSubscriber;

    @Inject
    BankSupportSelectionPresenter(Context applicationContext, User user) {
        this.applicationContext = applicationContext;
        this.mUser = user;
        mGetSupportBankSubscriber = new DefaultSubscriber<List<ZPBank>>() {
            @Override
            public void onError(Throwable e) {
                Timber.d("Get support bank  onError [%s]", e.getMessage());
                if (mView == null || getContext() == null) {
                    return;
                }
                mView.hideLoading();
                //showRetryDialog(mView.getActivity().getString(R.string.bank_error_load_supportbank));
                String message = ErrorMessageFactory.create(applicationContext, e);
                if (message.equals(getContext().getString(R.string.exception_generic))) {
                    message = getContext().getString(R.string.bank_error_load_supportbank);
                }
                if (e instanceof NetworkConnectionException) {
                    showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.NO_INTERNET);
                } else {
                    showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.ERROR_TYPE);
                }
            }

            @Override
            public void onNext(List<ZPBank> cardList) {
                Log.d(this, "get suport bank onComplete", cardList);
                if (mView == null || getContext() == null) {
                    return;
                }
                mView.hideLoading();
                fetchListBank(cardList);
            }
        };
    }

    private void showDialogThenClose(String error, @StringRes int cancelText, int dialogType) {
        if (mView == null) {
            return;
        }
        mView.showDialogThenClose(error, mView.getContext().getString(cancelText), dialogType);
    }

    @Override
    public void attachView(IBankSupportSelectionView iBankSupportSelectionView) {
        super.attachView(iBankSupportSelectionView);
    }

    @Override
    public void destroy() {
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
        return null;
    }

    private Context getContext() {
        if (mView == null)
            return null;
        return mView.getContext();
    }

    private void fetchListBank(List<ZPBank> listBank) {
        if (mView != null) {
            mView.fetchListBank(listBank);
        }
    }

    void getBankSupport() {
        Timber.d("start get bank support");
        Subscription subscription = SDKApplication
                .getApplicationComponent()
                .bankListInteractor()
                .getSupportBanks(BuildConfig.VERSION_NAME, System.currentTimeMillis())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> {
                    if(mView != null){
                        mView.showLoading();
                    }
                })
                .subscribe(mGetSupportBankSubscriber);
        mSubscription.add(subscription);

    }

    void linkCard() {
        setResultDoLinkCard();
    }

    private void setResultActivity(BankAction bankAction, BaseMap bankInfo) {
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

    private void setResultDoLinkCard() {
        getActivity().setResult(Constants.RESULT_DO_LINK_CARD);
        getActivity().finish();
    }

    @Override
    void onAddBankCardSuccess(MapCard bankCard) {
        setResultActivity(BankAction.LINK_CARD, bankCard);
    }

    @Override
    void onAddBankAccountSuccess(BankAccount bankAccount) {
        setResultActivity(BankAction.LINK_ACCOUNT, bankAccount);
    }

    @Override
    void onUnLinkBankAccountSuccess(BankAccount bankAccount) {
        setResultActivity(BankAction.UNLINK_ACCOUNT, bankAccount);
    }
}

//public class BankSupportSelectionPresenter extends AbstractBankPresenter<IBankSupportSelectionView> {
//    private final User mUser;
//    private final Context applicationContext;
//    private PaymentWrapper paymentWrapper;
//    private DefaultSubscriber<List<ZPBank>> mGetSupportBankSubscriber;
//
//    @Inject
//    BankSupportSelectionPresenter(Context applicationContext, User user) {
//        this.applicationContext = applicationContext;
//        this.mUser = user;
//        mGetSupportBankSubscriber = new DefaultSubscriber<List<ZPBank>>() {
//            @Override
//            public void onError(Throwable e) {
//                Timber.d("Get support bank  onError [%s]", e.getMessage());
//                if (mView == null || getContext() == null) {
//                    return;
//                }
//                mView.hideLoading();
//                //showRetryDialog(mView.getActivity().getString(R.string.bank_error_load_supportbank));
//                String message = ErrorMessageFactory.create(applicationContext, e);
//                if (message.equals(getContext().getString(R.string.exception_generic))) {
//                    message = getContext().getString(R.string.bank_error_load_supportbank);
//                }
//                if (e instanceof NetworkConnectionException) {
//                    showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.NO_INTERNET);
//                } else {
//                    showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.ERROR_TYPE);
//                }
//            }
//
//            @Override
//            public void onNext(List<ZPBank> cardList) {
//                Log.d(this, "get suport bank onComplete", cardList);
//                if (mView == null || getContext() == null) {
//                    return;
//                }
//                mView.hideLoading();
//                fetchListBank(cardList);
//            }
//        };
//
//        paymentWrapper = new PaymentWrapperBuilder()
//                .setResponseListener(new PaymentResponseListener())
//                .setLinkCardListener(new LinkCardListener(this))
//                .build();
//        paymentWrapper.initializeComponents();
//    }
//
//    private void showDialogThenClose(String error, @StringRes int cancelText, int dialogType) {
//        if (mView == null) {
//            return;
//        }
//        mView.showDialogThenClose(error, mView.getContext().getString(cancelText), dialogType);
//    }
//
//    @Override
//    public void attachView(IBankSupportSelectionView iBankSupportSelectionView) {
//        super.attachView(iBankSupportSelectionView);
//    }
//
//    @Override
//    public void destroy() {
//        paymentWrapper = null;
//        super.destroy();
//    }
//
//    @Override
//    Activity getActivity() {
//        if (mView == null)
//            return null;
//        return mView.getActivity();
//    }
//
//    @Override
//    User getUser() {
//        return mUser;
//    }
//
//    @Override
//    PaymentWrapper getPaymentWrapper() {
//        return paymentWrapper;
//    }
//
//    private Context getContext() {
//        if (mView == null)
//            return null;
//        return mView.getContext();
//    }
//
//    private void showRetryDialog(String message) {
//        if (mView == null || getContext() == null) {
//            return;
//        }
//
//        mView.showRetryDialog(message, new ZPWOnEventConfirmDialogListener() {
//            @Override
//            public void onCancelEvent() {
//                getActivity().finish();
//            }
//
//            @Override
//            public void onOKevent() {
//                getBankSupport();
//            }
//        });
//    }
//
//    private void fetchListBank(List<ZPBank> listBank) {
//        if (mView != null) {
//            mView.fetchListBank(listBank);
//        }
//    }
//
//    void getBankSupport() {
//        Timber.d("start get bank support");
//        Subscription subscription = SDKApplication
//                .getApplicationComponent()
//                .bankListInteractor()
//                .getSupportBanks(BuildConfig.VERSION_NAME, System.currentTimeMillis())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe(() -> {
//                    if(mView != null){
//                        mView.showLoading();
//                    }
//                })
//                .subscribe(mGetSupportBankSubscriber);
//        mSubscription.add(subscription);
//
//    }
//
//    void initData(Bundle bundle) {
//        if (bundle == null) {
//            return;
//        }
//        //mBankType = (LinkBankType) bundle.getSerializable(Constants.ARG_LINK_BANK_TYPE);
//    }
//
//    void linkCard() {
//        getPaymentWrapper().linkCard(getActivity());
////        setResultDoLinkCard();
//    }
//
//    private void setResultActivity(BankAction bankAction, BaseMap bankInfo) {
//        if (mView == null || bankInfo == null) {
//            return;
//        }
//
//        Bundle bundle = new Bundle();
//        Intent intent = new Intent();
//        bundle.putParcelable(Constants.BANK_DATA_RESULT_AFTER_LINK,
//                new BankInfo(bankAction, bankInfo.bankcode, bankInfo.getFirstNumber(), bankInfo.getLastNumber()));
//        intent.putExtras(bundle);
//        getActivity().setResult(Activity.RESULT_OK, intent);
//        getActivity().finish();
//    }
//
//    private void setResultErrorActivity(String message) {
//        Bundle bundle = new Bundle();
//        Intent intent = new Intent();
//        bundle.putString(Constants.BANK_DATA_RESULT_AFTER_LINK, message);
//        intent.putExtras(bundle);
//        getActivity().setResult(Activity.RESULT_CANCELED, intent);
//        getActivity().finish();
//    }
//
//    private void setResultDoLinkCard() {
//        getActivity().setResult(Constants.RESULT_DO_LINK_CARD);
//        getActivity().finish();
//    }
//
//    @Override
//    void onAddBankCardSuccess(MapCard bankCard) {
//        setResultActivity(BankAction.LINK_CARD, bankCard);
//    }
//
//    @Override
//    void onAddBankAccountSuccess(BankAccount bankAccount) {
//        setResultActivity(BankAction.LINK_ACCOUNT, bankAccount);
//    }
//
//    @Override
//    void onUnLinkBankAccountSuccess(BankAccount bankAccount) {
//        setResultActivity(BankAction.UNLINK_ACCOUNT, bankAccount);
//    }
//
//    protected void onErrorLinkCardButInputBankAccount(BaseMap bankInfo) {
//    }
//
//    // Inner class custom listener
//    private class PaymentResponseListener implements PaymentWrapper.IResponseListener {
//        @Override
//        public void onParameterError(String param) {
//            setResultErrorActivity(param);
//        }
//
//        @Override
//        public void onResponseError(PaymentError status) {
////            if (status == PaymentError.ERR_CODE_INTERNET) {
////                if (mView == null) {
////                    return;
////                }
////                mView.hideLoading();
////                mView.showNetworkErrorDialog();
////            }
//        }
//
//        @Override
//        public void onResponseSuccess(IBuilder builder) {
//            onResponseSuccessFromSDK(builder);
//        }
//
//        @Override
//        public void onResponseTokenInvalid() {
//            Timber.e("Invalid token");
//        }
//
//        @Override
//        public void onResponseAccountSuspended() {
//            Timber.e("Account suspended");
//        }
//
//        @Override
//        public void onAppError(String msg) {
//            setResultErrorActivity(msg);
//        }
//
//        @Override
//        public void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId) {
//
//        }
//    }
//
//    private class LinkCardListener implements PaymentWrapper.ILinkCardListener {
//        WeakReference<BankSupportSelectionPresenter> mWeakReference;
//
//        LinkCardListener(BankSupportSelectionPresenter presenter) {
//            mWeakReference = new WeakReference<>(presenter);
//        }
//
//        @Override
//        public void onErrorLinkCardButInputBankAccount(BaseMap bankInfo) {
//            if (mWeakReference.get() == null) {
//                return;
//            }
//
//            mWeakReference.get().onErrorLinkCardButInputBankAccount(bankInfo);
//        }
//    }
//}