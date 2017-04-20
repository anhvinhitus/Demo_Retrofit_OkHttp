package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.DownloadSDKResourceComplete;
import vn.com.vng.zalopay.event.LoadIconFontEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;

/**
 * Created by longlv on 10/25/16.
 * Common function of LinkCardPresenter & LinkAccountPresenter
 */

abstract class AbstractLinkCardPresenter<View> extends AbstractPresenter<View> {
    protected PaymentWrapper paymentWrapper;
    private Navigator mNavigator;
    private IGetCardSupportListListener mGetCardSupportListListener;
    boolean mPayAfterLinkAcc;

    User mUser;

    private SharedPreferences mSharedPreferences;

    protected EventBus mEventBus;

    abstract Activity getActivity();

    abstract Context getContext();

    abstract void onPreComplete();

    abstract void onAddCardSuccess(DBaseMap mappedCreditCard);

    abstract void onPayResponseError(PaymentError paymentError);

    abstract void onLoadIconFontSuccess();

    abstract void onDownloadPaymentSDKComplete();

    abstract void showLoadingView();

    abstract void hideLoadingView();

    abstract void showErrorView(String message);

    abstract void showNetworkErrorDialog();

    abstract void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener);

    abstract void onUpdateVersion(boolean forceUpdate, String latestVersion, String message);

    abstract void onGetCardSupportSuccess(ArrayList<ZPCard> cardSupportList);

    AbstractLinkCardPresenter(ZaloPayRepository zaloPayRepository,
                              Navigator navigator,
                              BalanceStore.Repository balanceRepository,
                              TransactionStore.Repository transactionRepository,
                              User user,
                              SharedPreferences sharedPreferences, EventBus eventBus) {
        mNavigator = navigator;
        this.mUser = user;
        mSharedPreferences = sharedPreferences;
        this.mEventBus = eventBus;
        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .build();

        mGetCardSupportListListener = new IGetCardSupportListListener() {
            @Override
            public void onProcess() {
                Timber.d("Get card support list on process");
            }

            @Override
            public void onComplete(ArrayList<ZPCard> cardSupportList) {
                Timber.d("Get card support onComplete : cardSupportList [%s]", cardSupportList);
                hideLoadingView();
                onGetCardSupportSuccess(cardSupportList);
            }

            @Override
            public void onError(String pErrorMess) {
                Timber.d("Get card support error : message [%s]", pErrorMess);
                hideLoadingView();
                showRetryDialog(getContext().getString(R.string.exception_generic),
                        new ZPWOnEventConfirmDialogListener() {
                            @Override
                            public void onCancelEvent() {

                            }

                            @Override
                            public void onOKevent() {
                                getListBankSupport();
                            }
                        });
            }

            @Override
            public void onUpVersion(boolean forceUpdate, String latestVersion, String message) {
                hideLoadingView();
                onUpdateVersion(forceUpdate, latestVersion, message);
            }
        };
    }

    @Override
    public void attachView(View view) {
        super.attachView(view);
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
    public void destroy() {
        //release cache
        CShareDataWrapper.dispose();
        super.destroy();
    }

    void initData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mPayAfterLinkAcc = bundle.getBoolean(Constants.ARG_CONTINUE_PAY_AFTER_LINK_ACC);
    }

    public void getListBankSupport(IGetCardSupportListListener listListener) {
        showLoadingView();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = mUser.zaloPayId;
        userInfo.accessToken = mUser.accesstoken;
        CShareDataWrapper.getCardSupportList(userInfo, listListener);
    }

    void getListBankSupport() {
        Timber.d("Show list bank that support link account.");
        getListBankSupport(mGetCardSupportListListener);
    }

    void addBankAccount() {
        if (getContext() == null) {
            return;
        }
        if (mUser.profilelevel < 2) {
            mNavigator.startUpdateProfileLevel2Activity(getContext());
        } else {
            getListBankSupport();
        }
    }

    void addLinkCard() {
        if (getContext() == null) {
            return;
        }
        if (mUser.profilelevel < 2) {
            mNavigator.startUpdateProfileLevel2Activity(getContext());
        } else {
            paymentWrapper.linkCard(getActivity());
            hideLoadingView();
            ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_ADDCARD_LAUNCH);
        }
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {

        @Override
        protected ILoadDataView getView() {
            if (mView instanceof ILoadDataView) {
                return (ILoadDataView) mView;
            }
            return null;
        }

        @Override
        public void onParameterError(String param) {
            showErrorView(param);
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                showNetworkErrorDialog();
            } else if (paymentError == PaymentError.ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT) {
                // Go to LinkBankActivity with page index = 1 (Tab "Liên kết tài khoản")
                mNavigator.startLinkAccountActivityAndFinish(getActivity());
            } else {
                onPayResponseError(paymentError);
            }
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (zpPaymentResult == null) {
                return;
            }
            ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
            if (paymentInfo == null || paymentInfo.mapBank == null) {
                Timber.d("On response success paymentInfo null");
                return;
            }
            onAddCardSuccess(paymentInfo.mapBank);
        }

        @Override
        public void onAppError(String msg) {
            showErrorView(msg);
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String tId, String pAppTransId) {
            Timber.d("onPreComplete : transactionId %s isSuccessful [%s] pAppTransId [%s]", tId, isSuccessful, pAppTransId);
            if (isSuccessful) {
                AbstractLinkCardPresenter.this.onPreComplete();
            }
        }

    }

    String detectCardType(String bankcode, String first6cardno) {
        if (TextUtils.isEmpty(bankcode)) {
            return ECardType.UNDEFINE.toString();
        } else if (bankcode.equals(ECardType.PVTB.toString())) {
            return ECardType.PVTB.toString();
        } else if (bankcode.equals(ECardType.PBIDV.toString())) {
            return ECardType.PBIDV.toString();
        } else if (bankcode.equals(ECardType.PVCB.toString())) {
            return ECardType.PVCB.toString();
        } else if (bankcode.equals(ECardType.PSCB.toString())) {
            return ECardType.PSCB.toString();
        } else if (bankcode.equals(ECardType.PSGCB.toString())) {
            return ECardType.PSGCB.toString();
        /*} else if (bankcode.equals(ECardType.PEIB.toString())) {
            return ECardType.PEIB.toString();
        } else if (bankcode.equals(ECardType.PAGB.toString())) {
            return ECardType.PAGB.toString();
        } else if (bankcode.equals(ECardType.PTPB.toString())) {
            return ECardType.PTPB.toString();*/
        } else if (bankcode.equals(ECardType.UNDEFINE.toString())) {
            return ECardType.UNDEFINE.toString();
        } else {

            UserInfo userInfo = new UserInfo();
            userInfo.zaloPayUserId = mUser.zaloPayId;
            userInfo.accessToken = mUser.accesstoken;

            try {
                return CShareDataWrapper.detectCardType(userInfo, first6cardno).toString();
            } catch (Exception e) {
                Timber.w(e, "detectCardType exception [%s]", e.getMessage());
            }
        }
        return ECardType.UNDEFINE.toString();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadIconFontSuccess(LoadIconFontEvent event) {
        mEventBus.removeStickyEvent(LoadIconFontEvent.class);
        onLoadIconFontSuccess();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadSDKResourceComplete(DownloadSDKResourceComplete event) {
        onDownloadPaymentSDKComplete();
    }
}
