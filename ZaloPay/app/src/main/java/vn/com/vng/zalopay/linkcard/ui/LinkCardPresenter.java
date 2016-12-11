package vn.com.vng.zalopay.linkcard.ui;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
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
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 5/11/16.
 * *
 */
public class LinkCardPresenter extends AbsLinkCardPresenter implements IPresenter<ILinkCardView> {

    private ILinkCardView mLinkCardView;

    @Inject
    User user;

    @Inject
    LinkCardPresenter(ZaloPayRepository zaloPayRepository,
                      Navigator navigator,
                      BalanceStore.Repository balanceRepository,
                      TransactionStore.Repository transactionRepository) {
        super(zaloPayRepository, navigator, balanceRepository, transactionRepository);
    }

    @Override
    public void attachView(ILinkCardView iLinkCardView) {
        mLinkCardView = iLinkCardView;
    }

    @Override
    public void detachView() {
        mLinkCardView = null;
        unsubscribeIfNotNull(mCompositeSubscription);
    }

    private void getListCard() {
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
        getListCard();
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
                BankCard bankCard = new BankCard(mapCard.cardname, mapCard.first6cardno,
                        mapCard.last4cardno, mapCard.bankcode, mapCard.expiretime);
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

    @Override
    Activity getActivity() {
        if (mLinkCardView == null) {
            return null;
        }
        return mLinkCardView.getActivity();
    }

    @Override
    Context getContext() {
        if (mLinkCardView == null) {
            return null;
        }
        return mLinkCardView.getContext();
    }

    @Override
    void onTokenInvalid() {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.onTokenInvalid();
    }

    @Override
    void onPreComplete() {

    }

    @Override
    void onAddCardSuccess(DMappedCard mappedCreditCard) {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.onAddCardSuccess(mappedCreditCard);
    }

    @Override
    protected void showLoadingView() {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.showLoading();
    }

    @Override
    protected void hideLoadingView() {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.hideLoading();
    }

    @Override
    protected void showErrorView(String message) {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.hideLoading();
        mLinkCardView.showError(message);
    }

    @Override
    void showNetworkErrorDialog() {
        if (mLinkCardView == null) {
            return;
        }
        mLinkCardView.hideLoading();
        mLinkCardView.showNetworkErrorDialog();
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
                UserInfo userInfo = new UserInfo();
                userInfo.zaloPayUserId = user.zaloPayId;
                userInfo.accessToken = user.accesstoken;
                return CShareData.getInstance().setUserInfo(userInfo).
                        detectCardType(first6cardno).toString();
            } catch (Exception e) {
                Timber.w(e, "detectCardType exception [%s]", e.getMessage());
            }
        }
        return ECardType.UNDEFINE.toString();
    }
}
