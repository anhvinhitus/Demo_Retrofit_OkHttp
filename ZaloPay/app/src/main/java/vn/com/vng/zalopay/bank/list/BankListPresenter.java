package vn.com.vng.zalopay.bank.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.authentication.AuthenticationCallback;
import vn.com.vng.zalopay.authentication.AuthenticationDialog;
import vn.com.vng.zalopay.authentication.AuthenticationPassword;
import vn.com.vng.zalopay.authentication.Stage;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.RefreshBankAccountEvent;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.network.exception.HttpEmptyResponseException;
import vn.com.vng.zalopay.pw.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.pw.PaymentWrapperBuilder;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.utils.PasswordUtil;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
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
import vn.com.zalopay.wallet.repository.bank.BankStore;

/**
 * Created by hieuvm on 7/10/17.
 * *
 */

final class BankListPresenter extends AbstractPresenter<IBankListView> {

    protected final Context mContext;
    private final User mUser;
    private final PaymentWrapper mPaymentWrapper;
    private final Navigator mNavigator;
    private boolean mPayAfterLinkBank;
    private boolean mWithdrawAfterLinkBank;
    private boolean mGotoSelectBank = false;
    private String mLinkCardWithBankCode = "";
    private String mLinkAccountWithBankCode = "";
    private UserConfig mUserConfig;
    private EventBus mEventBus;
    private BankStore.Interactor mBankInteractor;

    @Inject
    BankListPresenter(Context context, UserConfig userConfig, User user, Navigator navigator, EventBus eventBus) {
        mContext = context;
        mUser = user;
        mNavigator = navigator;
        mUserConfig = userConfig;
        mPaymentWrapper = new PaymentWrapperBuilder()
                .setResponseListener(new PaymentResponseListener())
                .setLinkCardListener(new LinkCardListener(this))
                .setShowNotificationLinkCard(false)
                .build();
        mPaymentWrapper.initializeComponents();
        mBankInteractor = SDKApplication
                .getApplicationComponent()
                .bankListInteractor();
        mEventBus = eventBus;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void attachView(IBankListView iBankListView) {
        super.attachView(iBankListView);
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    void handleBundle(Fragment fragment, Bundle bundle) {
        if (bundle == null) {
            return;
        }

        initData(bundle);

        if (!TextUtils.isEmpty(mLinkCardWithBankCode)) {
            linkBank(Constants.LinkBank.LINK_CARD, "");
        } else if (!TextUtils.isEmpty(mLinkAccountWithBankCode)) {
            linkBank(Constants.LinkBank.LINK_ACCOUNT, mLinkAccountWithBankCode);
        } else if (mGotoSelectBank) {
            mNavigator.startBankSupport(fragment, Constants.REQUEST_CODE_SELECT_BANK);
        }
    }

    private void initData(Bundle bundle) {
        mPayAfterLinkBank = bundle.getBoolean(Constants.ARG_CONTINUE_PAY_AFTER_LINK_BANK);
        mWithdrawAfterLinkBank = bundle.getBoolean(Constants.ARG_CONTINUE_WITHDRAW_AFTER_LINK_BANK);
        mGotoSelectBank = bundle.getBoolean(Constants.ARG_GOTO_SELECT_BANK_IN_LINK_BANK);
        mLinkCardWithBankCode = bundle.getString(Constants.ARG_LINK_CARD_WITH_BANK_CODE);
        mLinkAccountWithBankCode = bundle.getString(Constants.ARG_LINK_ACCOUNT_WITH_BANK_CODE);
    }

    void loadView() {
        Subscription subscription = getCard(mUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::transformBaseMap)
                .subscribe(new DefaultSubscriber<List<BankData>>() {
                    @Override
                    public void onNext(List<BankData> data) {
                        if (mView != null) {
                            mView.setData(data);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            mView.setData(Collections.emptyList());
                        }
                    }
                });


        mSubscription.add(subscription);
    }

    void startBankSupport(Fragment f) {
        ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_TAPADDCARD);
        mNavigator.startBankSupport(f, Constants.REQUEST_CODE_SELECT_BANK);
    }

    private String getVCBMaintenanceMessage() {
        BankConfig bankConfig = mBankInteractor.getBankConfig(CardType.PVCB);
        if (bankConfig != null && bankConfig.isBankMaintenence(BankFunctionCode.LINK_BANK_ACCOUNT)) {
            return bankConfig.getMaintenanceMessage(BankFunctionCode.LINK_BANK_ACCOUNT);
        }
        return null;
    }

    void confirmAndRemoveBank(BankData bankData) {
        if (!NetworkHelper.isNetworkAvailable(mView.getContext())) {
            mView.close(bankData);
            mView.showNetworkErrorDialog();
            return;
        }

        ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_DELETECARD);

        if (bankData.mBaseMap instanceof MapCard) {
            confirmAndRemoveCard(Constants.LinkBank.LINK_CARD, bankData);
        } else if (bankData.mBaseMap instanceof BankAccount) {
            confirmAndUnlinkBank(bankData);
        }
    }

    void unlinkBank(BankData bankData) {
        mPaymentWrapper.unlinkAccount((Activity) mView.getContext(), bankData.mBaseMap.bankcode);
    }

    void removeCard(BankData bankData) {
        MapCard mapCard = (MapCard) bankData.mBaseMap;
        SDKApplication.getApplicationComponent()
                .linkInteractor()
                .removeMap(mUser.zaloPayId, mUser.accesstoken, mapCard.cardname, mapCard.first6cardno, mapCard.last4cardno, mapCard.bankcode, BuildConfig.VERSION_NAME)
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(new UnLinkBankSubscriber(bankData));
    }

    private void confirmAndUnlinkBank(BankData account) {
        Subscription subscription = ObservableHelper.makeObservable(this::getVCBMaintenanceMessage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MaintainBankSubscriber(account));

        mSubscription.add(subscription);
    }

    void confirmAndRemoveCard(int type, BankData bankData) {
        if (mView.getContext() == null) {
            return;
        }
        if (type == Constants.LinkBank.LINK_ACCOUNT) {
            unlinkBank(bankData);
        } else if (type == Constants.LinkBank.LINK_CARD) {
            showAuthenticationDialog(bankData);
        }
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != Constants.REQUEST_CODE_SELECT_BANK) {
            return;
        }

        switch (resultCode) {
            case Activity.RESULT_OK:
                linkBank(data);
                break;
            case Activity.RESULT_CANCELED:
                break;
        }
    }

    private void linkBank(int type, String cardCode) {
        Timber.i("Link bank [type:%s cardCode:%s]", type, cardCode);
        if (mView == null || mView.getContext() == null) {
            Timber.d("ignore link bank");
            return;
        }

        if (type == Constants.LinkBank.LINK_CARD) {
            mPaymentWrapper.linkCard((Activity) mView.getContext(),cardCode);
        } else if (type == Constants.LinkBank.LINK_ACCOUNT) {
            if (TextUtils.isEmpty(cardCode)) {
                return;
            }

            mPaymentWrapper.linkAccount((Activity) mView.getContext(), cardCode);
        }
    }

    private void linkBank(Intent data) {
        if (data == null) {
            return;
        }

        int type = data.getIntExtra("type", -1);
        String cardCode = data.getStringExtra(Constants.BANK_DATA_RESULT_AFTER_LINK);
        linkBank(type, cardCode);
    }

    private List<BankData> transformBaseMap(List<BaseMap> baseMaps) {
        List<BankData> data = new ArrayList<>();
        for (BaseMap baseMap : baseMaps) {
            if (baseMap instanceof MapCard) {
                data.add(new BankData((MapCard) baseMap));
                continue;
            }

            if (baseMap instanceof BankAccount) {
                data.add(new BankData((BankAccount) baseMap, String.valueOf(mUser.phonenumber)));
            }
        }
        return data;
    }

    private Observable<List<BaseMap>> getCard(final User user) {
        return ObservableHelper.makeObservable(() -> {
            List<BaseMap> linkedBankList = new ArrayList<>();
            List<MapCard> linkedCardList = CShareDataWrapper.getMappedCardList(user);
            List<BankAccount> linkedAccList = CShareDataWrapper.getMapBankAccountList(user);

            if (!Lists.isEmptyOrNull(linkedCardList)) {
                linkedBankList.addAll(linkedCardList);
            }
            if (!Lists.isEmptyOrNull(linkedAccList)) {
                linkedBankList.addAll(linkedAccList);
            }

            Collections.sort(linkedBankList, (item1, item2) -> item1.displayorder > item2.displayorder ? 1 : -1);
            return linkedBankList;
        });
    }

    protected void onErrorLinkCardButInputBankAccount(BaseMap bankInfo) {
        if (bankInfo == null) {
            return;
        }
        Timber.d("Start LinkAccount with bank code [%s]", bankInfo.bankcode);
        List<BankAccount> bankAccounts = CShareDataWrapper.getMapBankAccountList(mUser);
        if (BankUtils.isLinkedBankAccount(bankAccounts, bankInfo.bankcode)) {
            String bankName = BankUtils.getBankName(bankInfo.bankcode);
            String message;
            if (!TextUtils.isEmpty(bankName)) {
                message = mContext.getString(R.string.bank_account_has_linked, bankName);
            } else {
                message = mContext.getString(R.string.bank_account_has_linked_this_bank);
            }

            showErrorView(message);
        } else {
            AndroidUtils.runOnUIThread(() -> linkBank(Constants.LinkBank.LINK_ACCOUNT, bankInfo.bankcode), 300);
        }
    }

    protected void showErrorView(String message) {
        if (mView != null) {
            mView.showError(message);
        }
    }

    private void showConfirmPayAfterLinkBank(BaseMap bankInfo) {
        if (mView == null) {
            return;
        }
        String message = mContext.getString(R.string.confirm_continue_pay_after_link_card);
        if (bankInfo instanceof BankAccount) {
            message = mContext.getString(R.string.confirm_continue_pay_after_link_account);
        }
        mView.showConfirmDialogAfterLinkBank(message);
    }

    private void showConfirmWithdrawAfterLinkBank(BaseMap bankInfo) {
        if (mView == null) {
            return;
        }

        String message = mContext.getString(R.string.confirm_continue_withdraw_after_link_card);
        if (bankInfo instanceof BankAccount) {
            message = mContext.getString(R.string.confirm_continue_withdraw_after_link_account);
        }
        mView.showConfirmDialogAfterLinkBank(message);
    }

    void onUnLinkBankAccountSuccess(BankAccount bankAccount) {
        BankData bankData = new BankData(bankAccount, String.valueOf(mUser.phonenumber));
        if (mView != null) {
            mView.remove(bankData);
        }
    }

    private int getInsertPosition(BankData pBankData) {
        if(mView == null){
            return 0;
        }
        List<BankData> bankDataList = mView.getData();
        if (Lists.isEmptyOrNull(bankDataList)) {
            return 0;
        }
        if (pBankData == null) {
            return 0;
        }
        String bankCode = pBankData.mBaseMap.bankcode;
        if (TextUtils.isEmpty(bankCode)) {
            return 0;
        }
        BankConfig bankConfig = mBankInteractor.getBankConfig(bankCode);
        if (bankConfig == null) {
            return 0;
        }
        int position = 0;
        for (BankData bankData : bankDataList) {
            if (bankData == null) {
                continue;
            }
            if (bankData.mBaseMap.displayorder >= bankConfig.displayorder) {
                break;
            }
            position++;
        }
        return position;
    }

    private void onAddBankSuccess(BankData bankData) {
        if (mView != null) {
            int pos = getInsertPosition(bankData);
            Timber.d("insert map bank into %s", pos);
            mView.insert(pos, bankData);
        }
        if (mPayAfterLinkBank) {
            showConfirmPayAfterLinkBank(bankData.mBaseMap);
        } else if (mWithdrawAfterLinkBank) {
            showConfirmWithdrawAfterLinkBank(bankData.mBaseMap);
        }
    }

    void onResponseSuccessFromSDK(IBuilder builder) {
        if (builder == null) {
            Timber.d("Payment SDK response success but payment builder null");
            return;
        }

        if (!AndroidUtils.isMainThread()) {
            AndroidUtils.runOnUIThread(() -> onResponseSuccessFromSDK(builder));
            return;
        }

        BaseMap baseMap = builder.getMapBank();

        if (baseMap instanceof MapCard) {
            onAddBankSuccess(new BankData((MapCard) baseMap));
            return;
        }

        if (baseMap instanceof BankAccount) {

            if (builder.isLinkAccount()) {
                onAddBankSuccess(new BankData((BankAccount) baseMap, String.valueOf(mUser.phonenumber)));
            } else if (builder.isUnLinkAccount()) {
                onUnLinkBankAccountSuccess((BankAccount) baseMap);
            }

            return;
        }

        Timber.w("Response success from sdk : BaseMap - other type: %s", baseMap);
    }

    private void showAuthenticationDialog(BankData bankData) {
        if (mView.getContext() == null) {
            Timber.w("showPasswordDialog get Context = null");
            return;
        }
        if (PasswordUtil.detectShowFingerPrint((Activity) mView.getContext(), mUserConfig)) {
            AuthenticationDialog dialog = AuthenticationDialog.newInstance();
            dialog.setStage(Stage.FINGERPRINT_DECRYPT);
            dialog.setAuthenticationCallback(new AuthenticationCallback() {
                @Override
                public void onAuthenticated(String password) {
                    removeCard(bankData);
                }

                @Override
                public void onShowPassword() {
                    showPassword(mView.getContext(), bankData);
                }
            });
            dialog.show(((Activity) mView.getContext()).getFragmentManager(), AuthenticationDialog.TAG);
        } else {
            showPassword(mView.getContext(), bankData);
        }
    }

    protected void showPassword(Context pContext, BankData bankData) {
        String message = pContext.getString(R.string.txt_title_remove_card);
        AuthenticationPassword authenticationPassword = new AuthenticationPassword(pContext, PasswordUtil.detectSuggestFingerprint(pContext, mUserConfig), new AuthenticationCallback() {
            @Override
            public void onAuthenticated(String password) {
                removeCard(bankData);
            }

            @Override
            public void onAuthenticationFailure() {
                Timber.d(" Authentication password fail");
            }
        });
        authenticationPassword.initialize();
        try {
            authenticationPassword.getPasswordManager().setTitle(message);
        } catch (Exception e) {
            Timber.d("Set title password error [%s]", e.getMessage());
        }

    }

    private static class LinkCardListener implements PaymentWrapper.ILinkCardListener {

        WeakReference<BankListPresenter> mPresenter;

        LinkCardListener(BankListPresenter presenter) {
            mPresenter = new WeakReference<>(presenter);
        }

        @Override
        public void onErrorLinkCardButInputBankAccount(BaseMap bankInfo) {
            if (mPresenter.get() == null) {
                return;
            }

            try {
                mPresenter.get().onErrorLinkCardButInputBankAccount(bankInfo);
            } catch (Exception e) {
                Timber.d(e);
            }
        }
    }

    private class MaintainBankSubscriber extends DefaultSubscriber<String> {
        BankData mBankAccount;

        MaintainBankSubscriber(BankData bankData) {
            mBankAccount = bankData;
        }

        @Override
        public void onNext(String maintainMessage) {
            if (TextUtils.isEmpty(maintainMessage)) {
                confirmAndRemoveCard(Constants.LinkBank.LINK_ACCOUNT, mBankAccount);
                return;
            }

            if (mView == null) {
                return;
            }

            mView.close(mBankAccount);
            mView.showNotificationDialog(maintainMessage);
        }

        @Override
        public void onError(Throwable e) {
            if (mView == null) {
                return;
            }

            mView.close(mBankAccount);
            mView.showError(ErrorMessageFactory.create(mContext, e));
        }
    }

    private class UnLinkBankSubscriber extends DefaultSubscriber<BaseResponse> {
        BankData mBankData;

        UnLinkBankSubscriber(BankData bankData) {
            mBankData = bankData;
        }

        @Override
        public void onStart() {
            if (mView != null) {
                mView.showLoading();
            }
        }

        @Override
        public void onNext(BaseResponse response) {

            if (response == null) {
                onError(new HttpEmptyResponseException());
                return;
            }

            if (response.returncode != 0) {
                onError(new BodyException(response.returncode, response.returnmessage));
                return;
            }

            if (mView == null) {
                return;
            }

            mView.hideLoading();
            mView.remove(mBankData);
            mView.showNotificationDialog(mContext.getString(R.string.txt_remove_link_successfully));
        }

        @Override
        public void onCompleted() {
            if (mView != null) {
                mView.hideLoading();
            }
        }

        @Override
        public void onError(Throwable e) {
            if (mView == null) {
                return;
            }

            mView.hideLoading();
            if (NetworkHelper.isNetworkAvailable(mContext)) {
                mView.showNetworkErrorDialog();
            } else {
                mView.showError(ErrorMessageFactory.create(mContext, e));
            }
        }
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {

        PaymentResponseListener() {
        }

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
            if (paymentError != PaymentError.ERR_CODE_INTERNET) {
                return;
            }

            if (mView != null) {
                mView.showNetworkErrorDialog();
            }
        }

        @Override
        public void onResponseSuccess(IBuilder builder) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshBankAccount(RefreshBankAccountEvent event) {
        Timber.d("BankListPresenter reload  map card here");
        if (mView != null) {
            loadView();
        }

    }
}
