package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;

/**
 * Created by longlv on 1/17/17.
 * *
 */
class LinkAccountPresenter extends AbstractLinkCardPresenter<ILinkAccountView> {

    private IGetCardSupportListListener mGetCardSupportListListener;

    @Inject
    LinkAccountPresenter(ZaloPayRepository zaloPayRepository,
                         Navigator navigator,
                         BalanceStore.Repository balanceRepository,
                         TransactionStore.Repository transactionRepository,
                         User user,
                         SharedPreferences sharedPreferences, EventBus eventBus) {
        super(zaloPayRepository, navigator, balanceRepository, transactionRepository,
                user, sharedPreferences, eventBus);
        mGetCardSupportListListener = new IGetCardSupportListListener() {
            @Override
            public void onProcess() {
                Timber.d("getCardSupportList onProcess");
            }

            @Override
            public void onComplete(ArrayList<ZPCard> cardSupportList) {
                Timber.d("getCardSupportList onComplete cardSupportList[%s]", cardSupportList);
                hideLoadingView();
                if (cardSupportList == null || cardSupportList.size() <= 0) {
                    return;
                }
                ArrayList<ZPCard> cards = new ArrayList<>();
                for (ZPCard card : cardSupportList) {
                    if (card == null || card.isBankAccount()) {
                        showErrorView("Chưa có ngân hàng hỗ trợ liên kết tài khoản.");
                        return;
                    }
                    cards.add(card);
                }
                if (mView != null) {
                    mView.showListBankDialog(cards);
                }
            }

            @Override
            public void onError(String pErrorMess) {
                Timber.d("cardSupportHashMap onError [%s]", pErrorMess);
                hideLoadingView();
                showRetryDialog();
            }

            @Override
            public void onUpVersion(boolean forceUpdate, String latestVersion, String message) {
                hideLoadingView();
                if (mView == null) {
                    return;
                }
                mView.onEventUpdateVersion(forceUpdate, latestVersion, message);
            }
        };
    }

    private void getListAccount() {
        //showLoadingView();

    }

    @Override
    public void resume() {
        getListAccount();
    }

    void showListBankSupportLinkAcc() {
        Timber.d("Show list bank that support link account.");
        showLoadingView();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = mUser.zaloPayId;
        userInfo.accessToken = mUser.accesstoken;
        CShareData.getInstance().setUserInfo(userInfo).getCardSupportList(mGetCardSupportListListener);
    }

    private void onGetLinkAccountSuccess(List<BankAccount> list) {
        hideLoadingView();
        mView.setData(list);
    }

    void removeLinkAccount(BankAccount bankCard) {

    }

    @Override
    Activity getActivity() {
        if (mView == null) {
            return null;
        }
        return mView.getActivity();
    }

    @Override
    Context getContext() {
        if (mView == null) {
            return null;
        }
        return mView.getContext();
    }

    @Override
    void onPreComplete() {

    }

    @Override
    void onAddCardSuccess(DBaseMap mappedCreditCard) {
        if (mView == null) {
            return;
        }
        mView.onAddAccountSuccess(mappedCreditCard);
    }


    private void showRetryDialog() {
        if (mView == null) {
            return;
        }
        mView.showRetryDialog(mView.getContext().getString(R.string.exception_generic), new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {

            }

            @Override
            public void onOKevent() {
                showListBankSupportLinkAcc();
            }
        });
    }

    @Override
    protected void showLoadingView() {
        if (mView == null) {
            return;
        }
        mView.showLoading();
    }

    @Override
    protected void hideLoadingView() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
    }

    @Override
    protected void showErrorView(String message) {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
        mView.showError(message);
    }

    @Override
    void showNetworkErrorDialog() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
        mView.showNetworkErrorDialog();
    }

}
