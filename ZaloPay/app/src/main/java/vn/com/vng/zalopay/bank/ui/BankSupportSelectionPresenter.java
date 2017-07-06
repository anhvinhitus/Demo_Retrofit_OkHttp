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
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAction;
import vn.com.vng.zalopay.bank.models.BankInfo;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
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
        List<BankAccount> mapCardList = CShareDataWrapper.getMapBankAccountList(getUser());
        if (checkLinkedBankAccount(mapCardList, cardCode)) {
            showVCBWarningDialog();
        } else {
            showVCBConfirmDialog(cardCode);
        }
    }

    private void showVCBWarningDialog() {
        if (mView == null) return;
        SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);
        dialog.setTitleText(getActivity().getString(R.string.notification));
        dialog.setContentText(getActivity().getString(R.string.bank_link_account_vcb_exist));
        dialog.setConfirmText(getActivity().getString(R.string.txt_close));
        dialog.setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> dialog.dismiss());
        dialog.show();
    }

    private void showVCBConfirmDialog(String cardCode) {
        if (mView == null) return;
        SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);

        dialog.setTitleText(getActivity().getString(R.string.notification));
        dialog.setCancelText(getActivity().getString(R.string.txt_cancel));
        dialog.setContentText(getVCBWarningMessage());
        dialog.setConfirmText(getActivity().getString(R.string.accept));
        dialog.setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> {
            setResultDoLinkAccount(cardCode);
            dialog.dismiss();
        });
        dialog.show();
    }

    //Just to note, though, the Java compiler will automatically convert.
    //Ref: https://stackoverflow.com/questions/4965513/stringbuilder-vs-string-considering-replace
    private String getVCBWarningMessage() {
        return String.format(getActivity().getString(R.string.link_account_empty_bank_support_phone_require_hint),
                "<b>" + PhoneUtil.formatPhoneNumberWithDot(getUser().phonenumber) + "</b>") +
                "<br><br>" +
                getActivity().getString(R.string.link_account_empty_bank_support_balance_require_hint);
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