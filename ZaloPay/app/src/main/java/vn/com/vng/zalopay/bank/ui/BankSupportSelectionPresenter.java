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
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.merchant.entities.ZPBank;

/**
 * Created by Duke on 5/25/17.
 * List support bank.
 */
public class BankSupportSelectionPresenter extends AbstractBankPresenter<IBankSupportSelectionView> {
    private final User mUser;
    private final Context applicationContext;

    @Inject
    BankSupportSelectionPresenter(Context applicationContext, User user) {
        this.applicationContext = applicationContext;
        this.mUser = user;
    }

    private DefaultSubscriber<List<ZPBank>> mGetSupportBankSubscriber = new DefaultSubscriber<List<ZPBank>>() {
        @Override
        public void onError(Throwable e) {
            Timber.d("Get support bank  onError [%s]", e.getMessage());
            if (mView == null) {
                return;
            }
            mView.hideLoading();
            String message = ErrorMessageFactory.create(applicationContext, e);
            if (message.equals(applicationContext.getString(R.string.exception_generic))) {
                message = applicationContext.getString(R.string.bank_error_load_supportbank);
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
            if (mView == null) {
                return;
            }
            mView.hideLoading();
            fetchListBank(cardList);
        }
    };

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
        if (mView == null) {
            return null;
        }
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
                .compose(SchedulerHelper.applySchedulers())
                .doOnSubscribe(() -> {
                    if (mView != null) {
                        mView.showLoading();
                    }
                })
                .subscribe(mGetSupportBankSubscriber);
        mSubscription.add(subscription);

    }

    void linkCard() {
        setResultDoLinkCard();
    }

    void linkAccount(String cardCode) {
        setResultDoLinkAccount(cardCode);
    }

    private void setResultActivity(BankAction bankAction, BaseMap bankInfo) {
        if (mView == null || mView.getActivity() == null || bankInfo == null) {
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

    private void setResultDoLinkAccount(String cardCode) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        bundle.putString(Constants.BANK_DATA_RESULT_AFTER_LINK, cardCode);
        intent.putExtras(bundle);
        getActivity().setResult(Constants.RESULT_DO_LINK_ACCOUNT, intent);
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