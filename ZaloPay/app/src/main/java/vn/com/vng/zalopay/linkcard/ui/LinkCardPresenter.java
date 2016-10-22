package vn.com.vng.zalopay.linkcard.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 5/11/16.
 * *
 */
public class LinkCardPresenter extends BaseUserPresenter implements IPresenter<ILinkCardView> {
    private final String FIRST_OPEN_SAVE_CARD_KEY = "1st_open_save_card";

    private ILinkCardView mLinkCardView;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private PaymentWrapper paymentWrapper;
    private ZaloPayRepository zaloPayRepository;
    private Navigator mNavigator;

    @Inject
    SharedPreferences mSharedPreferences;

    @Inject
    User user;

    @Inject
    LinkCardPresenter(ZaloPayRepository zaloPayRepository,
                      Navigator navigator,
                      BalanceStore.Repository balanceRepository,
                      TransactionStore.Repository transactionRepository) {
        this.zaloPayRepository = zaloPayRepository;
        mNavigator = navigator;
        paymentWrapper = new PaymentWrapper(balanceRepository, this.zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mLinkCardView.getActivity();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                showErrorView(param);
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                    showErrorView("Vui lòng kiểm tra kết nối mạng và thử lại.");
                }
//                else {
//                    mView.showError("Lỗi xảy ra trong quá trình nạp tiền. Vui lòng thử lại sau.");
//                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
                if (paymentInfo == null) {
                    Timber.d("onResponseSuccess paymentInfo null");
                    return;
                }
                mLinkCardView.onAddCardSuccess(paymentInfo.mappedCreditCard);
            }

            @Override
            public void onResponseTokenInvalid() {
                mLinkCardView.onTokenInvalid();
            }

            @Override
            public void onAppError(String msg) {
                if (mLinkCardView == null || mLinkCardView.getContext() == null) {
                    return;
                }
                showErrorView(mLinkCardView.getContext().getString(R.string.exception_generic));
            }

            @Override
            public void onPreComplete(boolean isSuccessful, String tId, String pAppTransId) {

            }

            @Override
            public void onNotEnoughMoney() {

            }
        });
    }

    @Override
    public void setView(ILinkCardView iLinkCardView) {
        mLinkCardView = iLinkCardView;
    }

    @Override
    public void destroyView() {
        mLinkCardView = null;
        unsubscribeIfNotNull(mCompositeSubscription);
    }

    void getListCard() {
        showLoadingView();
        Subscription subscription = ObservableHelper.makeObservable(new Callable<List<BankCard>>() {
            @Override
            public List<BankCard> call() throws Exception {
                List<DMappedCard> mapCardLis = CShareData.getInstance().getMappedCardList(user.zaloPayId);
                return transform(mapCardLis);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LinkCardSubscriber());
        mCompositeSubscription.add(subscription);
    }


    private BankCard transform(DMappedCard card) {
        BankCard bankCard = null;

        if (card != null) {
            bankCard = new BankCard(card.cardname, card.first6cardno, card.last4cardno, card.bankcode, card.expiretime);
            try {
                Timber.d("transform card.first6cardno:%s", card.first6cardno);
                bankCard.type = detectCardType(card.bankcode, card.first6cardno);
                Timber.d("transform bankCard.type:%s", bankCard.type);
            } catch (Exception e) {
                Timber.e(e, "transform DMappedCard to BankCard exception [%s]", e.getMessage());
            }
        }

        return bankCard;
    }

    private List<BankCard> transform(List<DMappedCard> cards) {
        if (Lists.isEmptyOrNull(cards)) return Collections.emptyList();

        List<BankCard> list = new ArrayList<>();

        for (DMappedCard dMappedCard : cards) {
            BankCard bCard = transform(dMappedCard);
            if (bCard != null) {
                list.add(bCard);
            }
        }

        return list;
    }


    @Override
    public void resume() {
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        //release cache
        CShareData.dispose();
        GlobalData.initApplication(null);
    }

    private void onGetLinkCardSuccess(List<BankCard> list) {
        mLinkCardView.setData(list);
        hideLoadingView();
    }

    void removeLinkCard(BankCard bankCard) {
        showLoadingView();

        ZPWRemoveMapCardParams params = new ZPWRemoveMapCardParams();
        DMappedCard mapCard = new DMappedCard();
        mapCard.cardname = bankCard.cardname;
        mapCard.first6cardno = bankCard.first6cardno;
        mapCard.last4cardno = bankCard.last4cardno;
        mapCard.bankcode = bankCard.bankcode;

        if (user == null) {
            showErrorView("Thông tin người dùng không hợp lệ.");
            return;
        }
        params.accessToken = user.accesstoken;
        params.userID = String.valueOf(user.zaloPayId);
        params.mapCard = mapCard;

        WalletSDKApplication.removeCardMap(params, new RemoveMapCardListener());
    }

    private final class RemoveMapCardListener implements ZPWRemoveMapCardListener {
        @Override
        public void onSuccess(DMappedCard mapCard) {
            Timber.tag("LinkCardPresenter").d("removed map card: %s", mapCard);
            hideLoadingView();
            if (mapCard != null) {
                BankCard bankCard = new BankCard(mapCard.cardname, mapCard.first6cardno, mapCard.last4cardno, mapCard.bankcode, mapCard.expiretime);
                mLinkCardView.removeData(bankCard);
            }
        }

        @Override
        public void onError(BaseResponse pMessage) {
            Timber.tag("LinkCardPresenter").d("RemoveMapCard onError: " + pMessage);
            hideLoadingView();
            if (pMessage == null) {
                if (NetworkHelper.isNetworkAvailable(mLinkCardView.getContext())) {
                    showErrorView("Lỗi xảy ra trong quá trình hủy liên kết thẻ. Vui lòng thử lại sau.");
                } else {
                    showErrorView("Vui lòng kiểm tra kết nối mạng và thử lại.");
                }
            } else if (pMessage.returncode == NetworkError.TOKEN_INVALID) {
                ApplicationSession applicationSession = AndroidApplication.instance().getAppComponent().applicationSession();
                applicationSession.setMessageAtLogin(R.string.exception_token_expired_message);
                applicationSession.clearUserSession();
            } else if (!TextUtils.isEmpty(pMessage.returnmessage)) {
                Timber.tag("LinkCardPresenter").e("err removed map card " + pMessage.returnmessage);
                showErrorView(pMessage.returnmessage);
            }
        }
    }

    private final class LinkCardSubscriber extends DefaultSubscriber<List<BankCard>> {
        LinkCardSubscriber() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            Timber.e(e, "LinkCardSubscriber ");
        }

        @Override
        public void onNext(List<BankCard> bankCards) {
//            ArrayList<BankCard> tmp = new ArrayList<>();
//            tmp.add(new BankCard("Nguyen Van A", "213134", "1231", "123VCB",234324234));
            LinkCardPresenter.this.onGetLinkCardSuccess(bankCards);
        }
    }

    void addLinkCard() {
        if (user.profilelevel < 2) {
            mNavigator.startUpdateProfileLevel2Activity(mLinkCardView.getContext());
//        } else if (!isOpenedIntroActivity()) {
//            mLinkCardView.startIntroActivityForResult();
        } else {
            long value = 10000;
            if (mLinkCardView.getActivity() != null) {
                try {
                    value = CShareData.getInstance().getLinkCardValue();
                } catch (Exception e) {
                    Timber.e(e, "getLinkCardValue exception [%s]", e.getMessage());
                }
            }
            showLoadingView();
            String description = mLinkCardView.getContext().getString(R.string.save_card_description);
            Subscription subscription = zaloPayRepository.createwalletorder(BuildConfig.PAYAPPID, value, ETransactionType.LINK_CARD.toString(), user.zaloPayId, description)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CreateWalletOrderSubscriber());
            mCompositeSubscription.add(subscription);
        }
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {
        CreateWalletOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("CreateWalletOrderSubscriber onNext order: [%s]" + order);
            LinkCardPresenter.this.onCreateWalletOrderSuccess(order);
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
            LinkCardPresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        Timber.d("onCreateWalletOrderError exception: [%s]" + e);
        hideLoadingView();
        String message = ErrorMessageFactory.create(mLinkCardView.getContext(), e);
        showErrorView(message);
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.d("onCreateWalletOrderSuccess order: [%s]", order);
        paymentWrapper.linkCard(order);
        hideLoadingView();
    }

    private void showLoadingView() {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.showLoading();
    }

    private void hideLoadingView() {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.hideLoading();
    }

    private void showErrorView(String message) {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.hideLoading();
        mLinkCardView.showError(message);
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
        } else if (bankcode.equals(ECardType.PEIB.toString())) {
            return ECardType.PEIB.toString();
        } else if (bankcode.equals(ECardType.PSCB.toString())) {
            return ECardType.PSCB.toString();
        } else if (bankcode.equals(ECardType.PAGB.toString())) {
            return ECardType.PAGB.toString();
        } else if (bankcode.equals(ECardType.PTPB.toString())) {
            return ECardType.PTPB.toString();
        } else if (bankcode.equals(ECardType.UNDEFINE.toString())) {
            return ECardType.UNDEFINE.toString();
        } else {
            try {
                return CShareData.getInstance().detectCardType(first6cardno).toString();
            } catch (Exception e) {
                Timber.e(e, "detectCardType exception [%s]", e.getMessage());
            }
        }
        return ECardType.UNDEFINE.toString();
    }

    void setOpenedIntroActivity() {
        mSharedPreferences.edit().putBoolean(FIRST_OPEN_SAVE_CARD_KEY, true).apply();
    }

    private boolean isOpenedIntroActivity() {
        return mSharedPreferences.getBoolean(FIRST_OPEN_SAVE_CARD_KEY, false);
    }
}
