package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.bank.models.LinkBankType;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.pw.PaymentWrapperBuilder;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by Duke on 5/25/17.
 */
public class BankSupportSelectionPresenter extends AbstractPresenter<IBankSupportSelectionView> {
    private PaymentWrapper paymentWrapper;
    private final User mUser;
    private LinkBankType mBankType;
    private DefaultSubscriber<List<ZPCard>> mGetSupportBankSubscriber;

    @Inject
    BankSupportSelectionPresenter(User user,
                                  BalanceStore.Repository balanceRepository,
                                  TransactionStore.Repository transactionRepository,
                                  ZaloPayRepository zaloPayRepository) {
        this.mUser = user;
        mGetSupportBankSubscriber = new DefaultSubscriber<List<ZPCard>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.d("Get support bank type [%s] onError [%s]", mBankType, e.getMessage());
                showRetryDialog();
            }

            @Override
            public void onNext(List<ZPCard> cardList) {
                Timber.d("Get support bank type [%s] onComplete list card [%s]", mBankType, cardList);
                mView.showLoading();
                fetchListBank(cardList);
            }
        };

        paymentWrapper = new PaymentWrapperBuilder()
//                .setBalanceRepository(balanceRepository)
//                .setZaloPayRepository(zaloPayRepository)
//                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setLinkCardListener(new LinkCardListener(this))
                .build();
    }

    @Override
    public void attachView(IBankSupportSelectionView iBankSupportSelectionView) {
        super.attachView(iBankSupportSelectionView);
    }

    private Activity getActivity() {
        if (mView == null)
            return null;
        return mView.getActivity();
    }

    private Context getContext() {
        if (mView == null)
            return null;
        return mView.getContext();
    }

    private void showRetryDialog() {
        if (mView == null || getContext() == null) {
            return;
        }

        mView.showRetryDialog(getContext().getString(R.string.exception_generic), new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {

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

    void iniData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mBankType = (LinkBankType) bundle.getSerializable(Constants.ARG_LINK_BANK_TYPE);
    }

    void linkCard() {
        paymentWrapper.linkCard(getActivity());
    }

    void linkAccount(String cardCode) {
        List<DBankAccount> mapCardLis = CShareDataWrapper.getMapBankAccountList(mUser);
        if (checkLinkedBankAccount(transformBankAccount(mapCardLis), cardCode)) {
            paymentWrapper.linkAccount(getActivity(), cardCode);
        } else {
            showVCBWarningDialog();
        }
    }

    void showVCBWarningDialog() {
        if (mView == null) return;
        SweetAlertDialog dialog = new SweetAlertDialog(mView.getActivity(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);

        dialog.setTitleText(mView.getActivity().getString(R.string.notification));
        dialog.setCancelText(mView.getActivity().getString(R.string.txt_cancel));
        dialog.setContentText(getVCBWarningMessage());
        dialog.setConfirmText(mView.getActivity().getString(R.string.accept));
        dialog.setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> {
            paymentWrapper.linkAccount(mView.getActivity(), "ZPVCB");
            dialog.dismiss();
        });
        dialog.show();
    }

    String getVCBWarningMessage() {
        StringBuilder message = new StringBuilder();
        message.append(String.format(mView.getActivity().getString(R.string.link_account_empty_bank_support_phone_require_hint),
                "<b>" + PhoneUtil.formatPhoneNumberWithDot(mUser.phonenumber) + "</b>"));
        message.append("<br><br>");
        message.append(mView.getActivity().getString(R.string.link_account_empty_bank_support_balance_require_hint));

        return message.toString();
    }

    boolean checkLinkedBankAccount(List<BankAccount> listBankAccount, String bankCode) {
        if (Lists.isEmptyOrNull(listBankAccount)) {
            return false;
        }
        for (BankAccount bankAccount : listBankAccount) {
            if (bankAccount == null || TextUtils.isEmpty(bankAccount.mBankCode)) {
                continue;
            }
            if (bankAccount.mBankCode.equalsIgnoreCase(bankCode)) {
                return true;
            }
        }
        return false;
    }

    List<BankAccount> transformBankAccount(List<DBankAccount> bankAccounts) {
        if (Lists.isEmptyOrNull(bankAccounts)) return Collections.emptyList();

        List<BankAccount> list = new ArrayList<>();
        for (DBankAccount dBankAccount : bankAccounts) {
            BankAccount bankAccount = transformBankAccount(dBankAccount);
            if (bankAccount != null) {
                list.add(bankAccount);
            }
        }
        return list;
    }

    BankAccount transformBankAccount(DBankAccount dBankAccount) {
        if (dBankAccount == null) {
            return null;
        }

        //bankCode [ZPVCB] firstaccountno[012240] lastaccountno[2165]
        return new BankAccount(dBankAccount.firstaccountno,
                dBankAccount.lastaccountno,
                dBankAccount.bankcode);
    }

    private void showErrorView(String message) {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
        mView.showError(message);
    }

    // Inner class custom listener
    private class PaymentResponseListener implements PaymentWrapper.IResponseListener {
        @Override
        public void onParameterError(String param) {
//            showErrorView(param);
            Bundle bundle = new Bundle();
            Intent intent = new Intent();
            bundle.putString(Constants.BANK_DATA_RESULT_AFTER_LINK, param);
            intent.putExtras(bundle);
            getActivity().setResult(Activity.RESULT_CANCELED, intent);
            getActivity().finish();
        }

        @Override
        public void onResponseError(PaymentError status) {
            if (status == PaymentError.ERR_CODE_INTERNET) {
                if (mView == null) {
                    return;
                }
                mView.hideLoading();
                mView.showNetworkErrorDialog();
            }
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (mView == null) return;

            Bundle bundle = new Bundle();
            Intent intent = new Intent();
            bundle.putParcelable(Constants.BANK_DATA_RESULT_AFTER_LINK, zpPaymentResult);
            intent.putExtras(bundle);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
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
//            showErrorView(msg);
            Bundle bundle = new Bundle();
            Intent intent = new Intent();
            bundle.putString(Constants.BANK_DATA_RESULT_AFTER_LINK, msg);
            intent.putExtras(bundle);
            getActivity().setResult(Activity.RESULT_CANCELED, intent);
            getActivity().finish();
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
