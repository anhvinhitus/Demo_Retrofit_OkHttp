package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.functions.Action1;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.bank.models.BankAction;
import vn.com.vng.zalopay.bank.models.BankInfo;
import vn.com.vng.zalopay.data.ServerErrorMessage;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.User;
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
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.paymentinfo.IBuilder;

/**
 * Created by datnt10 on 5/25/17.
 * Handle bank logic and display ui
 */

class BankPresenter extends AbstractBankPresenter<IBankView> {

    protected EventBus mEventBus;
    private User mUser;
    private Navigator mNavigator;
    private PaymentWrapper mPaymentWrapper;
    private boolean mPayAfterLinkBank;
    private boolean mWithdrawAfterLinkBank;
    private boolean mGotoSelectBank = false;
    private String mLinkCardWithBankCode = "";
    private String mLinkAccountWithBankCode = "";
    private Action1<Throwable> removeMapCardException = throwable -> {
        Timber.d("Remove map card error : message [%s]", throwable);
        if (mView == null) {
            return;
        }
        hideLoadingView();
        if (NetworkHelper.isNetworkAvailable(mView.getContext())) {
            showErrorView(mView.getContext().getString(R.string.error_message_link_card_unknown_error));
        } else {
            showNetworkErrorDialog();
        }
    };

    @Inject
    BankPresenter(User user,
                  Navigator navigator,
                  EventBus eventBus) {
        this.mUser = user;
        this.mNavigator = navigator;
        this.mEventBus = eventBus;
        mPaymentWrapper = new PaymentWrapperBuilder()
                .setResponseListener(new PaymentResponseListener())
                .setLinkCardListener(new LinkCardListener(this))
                .build();
        mPaymentWrapper.initializeComponents();
    }

    private Action1<BaseResponse> removeCardSuccess(MapCard mapCard) {
        return response -> {
            hideLoadingView();
            if (response != null && response.returncode == 0) {
                Timber.d("removed map card: %s", mapCard);
                if (mView == null || mapCard == null) {
                    return;
                }
                mView.removeLinkedBank(mapCard);
                showNotificationDialog(R.string.txt_remove_link_successfully);
            } else if (response != null && !TextUtils.isEmpty(response.returnmessage)) {
                showErrorView(response.returnmessage);
                if (response.returncode == ServerErrorMessage.TOKEN_INVALID) {
                    mEventBus.postSticky(new TokenPaymentExpiredEvent());
                }
            } else {
                if (NetworkHelper.isNetworkAvailable(mView.getContext())) {
                    showErrorView(mView.getContext().getString(R.string.error_message_link_card_unknown_error));
                } else {
                    showNetworkErrorDialog();
                }
            }
        };
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

    @Override
    public void resume() {
        super.resume();
        getLinkedBank();
    }

    @Override
    void onAddBankCardSuccess(MapCard bankCard) {
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
    void onAddBankAccountSuccess(BankAccount bankAccount) {
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
    void onUnLinkBankAccountSuccess(BankAccount bankAccount) {
        if (mView != null) {
            mView.removeLinkedBank(bankAccount);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadIconFontSuccess(LoadIconFontEvent event) {
        mEventBus.removeStickyEvent(LoadIconFontEvent.class);
        if (mView != null) {
            mView.refreshLinkedBankList();
        }
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

    protected void linkCard() {
        getPaymentWrapper().linkCard(getActivity());
    }

    private void removeLinkedCard(MapCard mappCard) {
        if (mUser == null) {
            showErrorView("Thông tin người dùng không hợp lệ.");
            return;
        }
        showLoadingView();
        SDKApplication.getApplicationComponent()
                .linkInteractor()
                .removeMap(mUser.zaloPayId, mUser.accesstoken, mappCard.cardname, mappCard.first6cardno, mappCard.last4cardno, mappCard.bankcode, BuildConfig.VERSION_NAME)
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(removeCardSuccess(mappCard), removeMapCardException);
    }

    private void removeLinkedAccount(BankAccount bankAccount) {
        if (mPaymentWrapper == null || mView == null || bankAccount == null) {
            return;
        }
        mPaymentWrapper.unlinkAccount(mView.getActivity(), bankAccount.bankcode);
    }

    void removeLinkedBank(BaseMap item) {
        if (!NetworkHelper.isNetworkAvailable(mView.getContext())) {
            showNetworkErrorDialog();
            return;
        }
        if (item instanceof MapCard) {
            removeLinkedCard((MapCard) item);
        } else if (item instanceof BankAccount) {
            removeLinkedAccount((BankAccount) item);
        }
    }

    void linkAccount(String cardCode) {
        getPaymentWrapper().linkAccount(getActivity(), cardCode);
    }

    String VCBMaintenanceMessage() {
        BankConfig bankConfig = SDKApplication
                .getApplicationComponent()
                .bankListInteractor()
                .getBankConfig(CardType.PVCB);
        if (bankConfig != null && bankConfig.isBankMaintenence(BankFunctionCode.LINK_BANK_ACCOUNT)) {
            return bankConfig.getMaintenanceMessage(BankFunctionCode.LINK_BANK_ACCOUNT);
        }
        return null;
    }

//    private List<BaseMap> getFakeData() {
//        List<BaseMap> linkedBankList = new ArrayList<>();
//
//        MapCard visaCard = new MapCard();
//        visaCard.first6cardno = "445093";
//        visaCard.last4cardno = "0161";
//        visaCard.bankcode = CardType.MASTER.toString();
//        linkedBankList.add(visaCard);
//
//        MapCard vtbCard = new MapCard();
//        vtbCard.bankcode = CardType.PVTB.toString();
//        vtbCard.first6cardno = "970415";
//        vtbCard.last4cardno = "3538";
//        linkedBankList.add(vtbCard);
//
//        MapCard vcbCard = new MapCard();
//        vcbCard.bankcode = CardType.PVCB.toString();
//        vcbCard.first6cardno = "686868";
//        vcbCard.last4cardno = "1231";
//        linkedBankList.add(vcbCard);
//
//        MapCard sCard = new MapCard();
//        sCard.bankcode = CardType.PSCB.toString();
//        sCard.first6cardno = "970403";
//        sCard.last4cardno = "1234";
//        linkedBankList.add(sCard);
//
//        MapCard sgCard = new MapCard();
//        sgCard.bankcode = CardType.PSGCB.toString();
//        sgCard.first6cardno = "157979";
//        sgCard.last4cardno = "9999";
//        linkedBankList.add(sgCard);
//
//        MapCard bivdCard = new MapCard();
//        bivdCard.first6cardno = "970418";
//        bivdCard.last4cardno = "1231";
//        bivdCard.bankcode = CardType.PBIDV.toString();
//        linkedBankList.add(bivdCard);
//
//        BankAccount vcbAccount = new BankAccount();
//        vcbAccount.firstaccountno = "098765";
//        vcbAccount.lastaccountno = "4321";
//        vcbAccount.bankcode = CardType.PVCB.toString();
//        linkedBankList.add(vcbAccount);
//
//        return linkedBankList;
//    }

    void getLinkedBank() {
        List<BaseMap> linkedBankList = new ArrayList<>();
        List<MapCard> linkedCardList = CShareDataWrapper.getMappedCardList(mUser);
        List<BankAccount> linkedAccList = CShareDataWrapper.getMapBankAccountList(mUser);

        if (!Lists.isEmptyOrNull(linkedCardList)) {
            linkedBankList.addAll(linkedCardList);
        }
        if (!Lists.isEmptyOrNull(linkedAccList)) {
            linkedBankList.addAll(linkedAccList);
        }

        Collections.sort(linkedBankList, (item1, item2) -> Integer.valueOf(item1.displayorder).compareTo(item2.displayorder));

        mView.setListLinkedBank(linkedBankList);
//        mView.setListLinkedBank(getFakeData());
    }

    void smoothOpenItemMenu(int position) {
        mView.smoothOpenItemMenu(position);
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

    void AddMoreBank() {
        if (mView != null) {
            mNavigator.startBankSupportSelectionActivity(mView.getFragment());
        }
    }

    User getCurrentUser() {
        return mUser;
    }

    private void showConfirmPayAfterLinkBank(BaseMap bankInfo) {
        if (mView == null) {
            return;
        }
        String message = getString(R.string.confirm_continue_pay_after_link_card);
        if (bankInfo instanceof BankAccount) {
            message = getString(R.string.confirm_continue_pay_after_link_account);
        }
        mView.showConfirmDialogAfterLinkBank(message);
    }

    private void showConfirmWithdrawAfterLinkBank(BaseMap bankInfo) {
        if (mView == null) {
            return;
        }
        String message = getString(R.string.confirm_continue_withdraw_after_link_card);
        if (bankInfo instanceof BankAccount) {
            message = getString(R.string.confirm_continue_withdraw_after_link_account);
        }
        mView.showConfirmDialogAfterLinkBank(message);
    }

    void onAddBankSuccess(BankInfo bankInfo) {
        if (bankInfo == null) {
            return;
        }
        if (bankInfo.mBankAction == BankAction.LINK_CARD) {
            MapCard mappedCard = new MapCard();
            mappedCard.bankcode = bankInfo.mBankCode;
            mappedCard.first6cardno = bankInfo.mFirstNumber;
            mappedCard.last4cardno = bankInfo.mLastNumber;
            onAddBankCardSuccess(mappedCard);
        } else if (bankInfo.mBankAction == BankAction.LINK_ACCOUNT) {
            BankAccount dBankAccount = new BankAccount();
            dBankAccount.bankcode = bankInfo.mBankCode;
            dBankAccount.firstaccountno = bankInfo.mFirstNumber;
            dBankAccount.lastaccountno = bankInfo.mLastNumber;
            onAddBankAccountSuccess(dBankAccount);
        }
    }

    private void onErrorLinkCardButInputBankAccount(BaseMap bankInfo) {
        if (bankInfo == null) {
            return;
        }
        Timber.d("Start LinkAccount with bank code [%s]", bankInfo.bankcode);
        List<BankAccount> bankAccounts = CShareDataWrapper.getMapBankAccountList(mUser);
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
            new Handler().postDelayed(() -> linkAccount(bankInfo.bankcode), 300);
        }
    }

    private static class LinkCardListener implements PaymentWrapper.ILinkCardListener {

        WeakReference<BankPresenter> mWeakReference;

        LinkCardListener(BankPresenter presenter) {
            mWeakReference = new WeakReference<>(presenter);
        }

        @Override
        public void onErrorLinkCardButInputBankAccount(BaseMap bankInfo) {
            if (mWeakReference.get() == null) {
                return;
            }

            mWeakReference.get().onErrorLinkCardButInputBankAccount(bankInfo);
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
        public void onResponseSuccess(IBuilder builder) {
            if (builder == null) {
                return;
            }
            onResponseSuccessFromSDK(builder);
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

}
