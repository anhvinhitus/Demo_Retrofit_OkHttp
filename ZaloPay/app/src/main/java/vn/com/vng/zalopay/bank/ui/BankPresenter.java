package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.bank.models.BankAction;
import vn.com.vng.zalopay.bank.models.BankInfo;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.LoadIconFontEvent;
import vn.com.vng.zalopay.event.TokenPaymentExpiredEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.pw.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.pw.PaymentWrapperBuilder;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;

/**
 * Created by datnt10 on 5/25/17.
 * Handle bank logic and display ui
 */

class BankPresenter extends AbstractBankPresenter<IBankView> {

    private User mUser;
    private Navigator mNavigator;
    private PaymentWrapper mPaymentWrapper;
    protected EventBus mEventBus;
    private boolean mPayAfterLinkBank;
    private boolean mWithdrawAfterLinkBank;
    private boolean mGotoSelectBank = false;
    private String mLinkCardWithBankCode = "";
    private String mLinkAccountWithBankCode = "";

    @Inject
    BankPresenter(User user,
                  Navigator navigator,
                  EventBus eventBus,
                  ZaloPayRepository zaloPayRepository,
                  BalanceStore.Repository balanceRepository,
                  TransactionStore.Repository transactionRepository) {
        this.mUser = user;
        this.mNavigator = navigator;
        this.mEventBus = eventBus;
        mPaymentWrapper = new PaymentWrapperBuilder()
                .setResponseListener(new PaymentResponseListener())
                .setLinkCardListener(new LinkCardListener(this))
                .build();
    }

    @Override
    public void attachView(IBankView iBankView) {
        super.attachView(iBankView);
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void detachView() {
        mEventBus.unregister(this);
        super.detachView();
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
        return mPaymentWrapper;
    }

    void initData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mPayAfterLinkBank = bundle.getBoolean(Constants.ARG_CONTINUE_PAY_AFTER_LINK_BANK);
        mWithdrawAfterLinkBank = bundle.getBoolean(Constants.ARG_CONTINUE_WITHDRAW_AFTER_LINK_BANK);
        mGotoSelectBank = bundle.getBoolean(Constants.ARG_GOTO_SELECT_BANK_IN_LINK_BANK);
        mLinkCardWithBankCode = bundle.getString(Constants.ARG_LINK_CARD_WITH_BANK_CODE);
        mLinkAccountWithBankCode = bundle.getString(Constants.ARG_LINK_ACCOUNT_WITH_BANK_CODE);
    }

    void initPageStart() {
        if (!TextUtils.isEmpty(mLinkCardWithBankCode)) {
            linkCard();
        } else if (!TextUtils.isEmpty(mLinkAccountWithBankCode)) {
            linkAccount(mLinkAccountWithBankCode);
        } else if (mGotoSelectBank) {
            mNavigator.startBankSupportSelectionActivity(mView.getFragment());
        }
    }

    private void linkCard() {
        mPaymentWrapper.linkCard(getActivity());
    }

    void AddMoreBank() {
        if (mView != null) {
            mNavigator.startBankSupportSelectionActivity(mView.getFragment());
        }
    }

    private void removeLinkedCard(DMappedCard mappedCard) {
        showLoadingView();

        ZPWRemoveMapCardParams params = new ZPWRemoveMapCardParams();
        DMappedCard mapCard = new DMappedCard();
        mapCard.cardname = mappedCard.cardname;
        mapCard.first6cardno = mappedCard.first6cardno;
        mapCard.last4cardno = mappedCard.last4cardno;
        mapCard.bankcode = mappedCard.bankcode;

        if (mUser == null) {
            showErrorView("Thông tin người dùng không hợp lệ.");
            return;
        }
        params.accessToken = mUser.accesstoken;
        params.userID = String.valueOf(mUser.zaloPayId);
        params.mapCard = mapCard;

        SDKApplication.removeCardMap(params, new RemoveMapCardListener());
    }

    private void removeLinkedAccount(DBankAccount bankAccount) {
        if (mPaymentWrapper == null || mView == null || bankAccount == null) {
            return;
        }
        mPaymentWrapper.unlinkAccount(mView.getActivity(), bankAccount.bankcode);
    }

    void removeLinkedBank(DBaseMap item) {
        if (item instanceof DMappedCard) {
            removeLinkedCard((DMappedCard) item);
        } else if (item instanceof DBankAccount) {
            removeLinkedAccount((DBankAccount) item);
        }
    }

    /*private List<DBaseMap> getFakeData() {
        List<DBaseMap> linkedBankList = new ArrayList<>();

        DMappedCard visaCard = new DMappedCard();
        visaCard.first6cardno = "445093";
        visaCard.last4cardno = "0161";
        visaCard.bankcode = vn.com.zalopay.wallet.business.data.Constants.CCCode;
        linkedBankList.add(visaCard);

        DMappedCard vtbCard = new DMappedCard();
        vtbCard.bankcode = ECardType.PVTB.toString();
        vtbCard.first6cardno = "970415";
        vtbCard.last4cardno = "3538";
        linkedBankList.add(vtbCard);

        DMappedCard vcbCard = new DMappedCard();
        vcbCard.bankcode = ECardType.PVCB.toString();
        vcbCard.first6cardno = "686868";
        vcbCard.last4cardno = "1231";
        linkedBankList.add(vcbCard);

        DMappedCard sCard = new DMappedCard();
        sCard.bankcode = ECardType.PSCB.toString();
        sCard.first6cardno = "970403";
        sCard.last4cardno = "1234";
        linkedBankList.add(sCard);

        DMappedCard sgCard = new DMappedCard();
        sgCard.bankcode = ECardType.PSGCB.toString();
        sgCard.first6cardno = "157979";
        sgCard.last4cardno = "9999";
        linkedBankList.add(sgCard);

        DMappedCard bivdCard = new DMappedCard();
        bivdCard.first6cardno = "970418";
        bivdCard.last4cardno = "1231";
        bivdCard.bankcode = ECardType.PBIDV.toString();
        linkedBankList.add(bivdCard);

        DBankAccount vcbAccount = new DBankAccount();
        vcbAccount.firstaccountno = "098765";
        vcbAccount.lastaccountno = "4321";
        vcbAccount.bankcode = ECardType.PVCB.toString();
        linkedBankList.add(vcbAccount);

        return linkedBankList;
    }*/

    void getLinkedBank() {
        List<DBaseMap> linkedBankList = new ArrayList<>();
        List<DMappedCard> linkedCardList = CShareDataWrapper.getMappedCardList(mUser);
        List<DBankAccount> linkedAccList = CShareDataWrapper.getMapBankAccountList(mUser);

        if (!Lists.isEmptyOrNull(linkedCardList)) {
            linkedBankList.addAll(linkedCardList);
        }
        if (!Lists.isEmptyOrNull(linkedAccList)) {
            linkedBankList.addAll(linkedAccList);
        }

        mView.setListLinkedBank(linkedBankList);
//        mView.setListLinkedBank(getFakeData());
    }

    private String getString(@StringRes int stringResource) {
        if (mView == null || mView.getContext() == null) {
            return "";
        }
        return mView.getContext().getString(stringResource);
    }

    private void showNetworkErrorDialog() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
        mView.showNetworkErrorDialog();
    }

    private void showNotificationDialog(int msgResource) {
        if (mView != null) {
            mView.hideLoading();
            mView.showNotificationDialog(getString(msgResource));
        }
    }

    private void showErrorView(String message) {
        if (mView != null) {
            mView.showError(message);
        }
    }

    private void showLoadingView() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    private void hideLoadingView() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    User getCurrentUser() {
        return mUser;
    }

    private final class RemoveMapCardListener implements ZPWRemoveMapCardListener {
        @Override
        public void onSuccess(DMappedCard mapCard) {
            Timber.d("removed map card: %s", mapCard);
            hideLoadingView();
            if (mView == null || mapCard == null) {
                return;
            }
            mView.removeLinkedBank(mapCard);
            showNotificationDialog(R.string.txt_remove_link_successfully);
        }

        @Override
        public void onError(BaseResponse pMessage) {
            Timber.d("Remove map card error : message [%s]", pMessage);
            if (mView == null) {
                return;
            }

            hideLoadingView();
            if (pMessage == null) {
                if (NetworkHelper.isNetworkAvailable(mView.getContext())) {
                    showErrorView(mView.getContext().getString(R.string.error_message_link_card_unknown_error));
                } else {
                    showNetworkErrorDialog();
                }
            }
            // TODO just comment for build - need to recheck
//            else if (pMessage.returncode == NetworkError.TOKEN_INVALID) {
//                mEventBus.postSticky(new TokenPaymentExpiredEvent());
//            }
            else if (!TextUtils.isEmpty(pMessage.returnmessage)) {
                Timber.d("err removed map card %s", pMessage.returnmessage);
                showErrorView(pMessage.returnmessage);
            }
        }
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {

        @Override
        protected ILoadDataView getView() {
            return mView;
        }

        @Override
        public void onParameterError(String param) {
            showErrorView(param);
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                showNetworkErrorDialog();
            }
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (zpPaymentResult == null) {
                return;
            }
            onResponseSuccessFromSDK(zpPaymentResult);
        }

        @Override
        public void onAppError(String msg) {
            showErrorView(msg);
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String tId, String pAppTransId) {
            Timber.d("onPreComplete payment, transactionId %s isSuccessful [%s] pAppTransId [%s]", tId, isSuccessful, pAppTransId);
        }
    }

    private void showConfirmPayAfterLinkBank(DBaseMap bankInfo) {
        if (mView == null) {
            return;
        }
        String message = getString(R.string.confirm_continue_pay_after_link_card);
        if (bankInfo instanceof DBankAccount) {
            message = getString(R.string.confirm_continue_pay_after_link_account);
        }
        mView.showConfirmDialogAfterLinkBank(message);
    }

    private void showConfirmWithdrawAfterLinkBank(DBaseMap bankInfo) {
        if (mView == null) {
            return;
        }
        String message = getString(R.string.confirm_continue_withdraw_after_link_card);
        if (bankInfo instanceof DBankAccount) {
            message = getString(R.string.confirm_continue_withdraw_after_link_account);
        }
        mView.showConfirmDialogAfterLinkBank(message);
    }

    @Override
    void onAddBankCardSuccess(DMappedCard bankCard) {
        if (bankCard != null) {
            mView.onAddBankSuccess(bankCard);
        }
        if (mPayAfterLinkBank) {
            showConfirmPayAfterLinkBank(bankCard);
        } else if (mWithdrawAfterLinkBank) {
            showConfirmWithdrawAfterLinkBank(bankCard);
        }
    }

    @Override
    void onAddBankAccountSuccess(DBankAccount bankAccount) {
        if (bankAccount != null) {
            mView.onAddBankSuccess(bankAccount);
        }
        if (mPayAfterLinkBank) {
            showConfirmPayAfterLinkBank(bankAccount);
        } else if (mWithdrawAfterLinkBank) {
            showConfirmWithdrawAfterLinkBank(bankAccount);
        }
    }

    @Override
    void onUnLinkBankAccountSuccess(DBankAccount bankAccount) {
        if (mView != null) {
            mView.removeLinkedBank(bankAccount);
        }
    }

    void onAddBankSuccess(BankInfo bankInfo) {
        if (bankInfo == null) {
            return;
        }
        if (bankInfo.mBankAction == BankAction.LINK_CARD) {
            DMappedCard mappedCard = new DMappedCard();
            mappedCard.bankcode = bankInfo.mBankCode;
            mappedCard.first6cardno = bankInfo.mFirstNumber;
            mappedCard.last4cardno = bankInfo.mLastNumber;
            onAddBankCardSuccess(mappedCard);
        } else if (bankInfo.mBankAction == BankAction.LINK_ACCOUNT) {
            DBankAccount dBankAccount = new DBankAccount();
            dBankAccount.bankcode = bankInfo.mBankCode;
            dBankAccount.firstaccountno = bankInfo.mFirstNumber;
            dBankAccount.lastaccountno = bankInfo.mLastNumber;
            onAddBankAccountSuccess(dBankAccount);
        }
    }

    private void onErrorLinkCardButInputBankAccount(DBaseMap bankInfo) {
        if (bankInfo == null) {
            return;
        }
        Timber.d("Start LinkAccount with bank code [%s]", bankInfo.bankcode);
        List<DBankAccount> bankAccounts = CShareDataWrapper.getMapBankAccountList(mUser);
        if (checkLinkedBankAccount(bankAccounts, bankInfo.bankcode)) {
            String bankName = BankUtils.getBankName(bankInfo.bankcode);
            String message;
            if (!TextUtils.isEmpty(bankName)) {
                message = String.format(getString(R.string.bank_account_has_linked),
                        bankName);
            } else {
                message = getString(R.string.bank_account_has_linked_this_bank);
            }
            showErrorView(message);
        } else {
            linkAccount(bankInfo.bankcode);
        }
    }

    private static class LinkCardListener implements PaymentWrapper.ILinkCardListener {

        WeakReference<BankPresenter> mWeakReference;

        LinkCardListener(BankPresenter presenter) {
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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadIconFontSuccess(LoadIconFontEvent event) {
        mEventBus.removeStickyEvent(LoadIconFontEvent.class);
        if (mView != null) {
            mView.refreshLinkedBankList();
        }
    }

}
