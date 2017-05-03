package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.bank.models.LinkBankType;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.TokenPaymentExpiredEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by AnhHieu on 5/11/16.
 * *
 */
public class LinkCardPresenter extends AbstractLinkCardPresenter<ILinkCardView> {

    @Inject
    LinkCardPresenter(ZaloPayRepository zaloPayRepository,
                      Navigator navigator,
                      BalanceStore.Repository balanceRepository,
                      TransactionStore.Repository transactionRepository,
                      User user, EventBus eventBus) {
        super(zaloPayRepository, navigator, balanceRepository, transactionRepository, user, eventBus);
    }

    void getListCard() {
        List<DMappedCard> mapCardLis = CShareDataWrapper.getMappedCardList(mUser.zaloPayId);
        mView.setData(transformBankCard(mapCardLis));
    }

    @Override
    public void resume() {
    }

    void removeLinkCard(BankCard bankCard) {
        showLoadingView();

        ZPWRemoveMapCardParams params = new ZPWRemoveMapCardParams();
        DMappedCard mapCard = new DMappedCard();
        mapCard.cardname = bankCard.cardname;
        mapCard.first6cardno = bankCard.first6cardno;
        mapCard.last4cardno = bankCard.last4cardno;
        mapCard.bankcode = bankCard.bankcode;

        if (mUser == null) {
            showErrorView("Thông tin người dùng không hợp lệ.");
            return;
        }
        params.accessToken = mUser.accesstoken;
        params.userID = String.valueOf(mUser.zaloPayId);
        params.mapCard = mapCard;

        SDKApplication.removeCardMap(params, new RemoveMapCardListener());
    }

    private final class RemoveMapCardListener implements ZPWRemoveMapCardListener {
        @Override
        public void onSuccess(DMappedCard mapCard) {
            Timber.d("removed map card: %s", mapCard);
            if (mView == null) {
                return;
            }

            hideLoadingView();
            if (mapCard != null) {
                BankCard bankCard = new BankCard(mapCard.cardname, mapCard.first6cardno,
                        mapCard.last4cardno, mapCard.bankcode);
                mView.removeData(bankCard);
            }
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
            } else if (pMessage.returncode == NetworkError.TOKEN_INVALID) {
                mEventBus.postSticky(new TokenPaymentExpiredEvent());
            } else if (!TextUtils.isEmpty(pMessage.returnmessage)) {
                Timber.d("err removed map card %s", pMessage.returnmessage);
                showErrorView(pMessage.returnmessage);
            }
        }
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
    void onResponseSuccessFromSDK(ZPPaymentResult zpPaymentResult) {
        ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
        if (paymentInfo == null) {
            Timber.d("PaymentSDK response success but paymentInfo null");
            return;
        }
        onAddCardSuccess(paymentInfo.mapBank);
    }

    private void onAddCardSuccess(DBaseMap mapBank) {
        if (mView == null || mapBank == null) {
            return;
        }
        if (mapBank instanceof DMappedCard) {
            mView.onAddCardSuccess(mapBank);
            if (mPayAfterLinkBank) {
                mView.showConfirmPayAfterLinkBank(LinkBankType.LINK_BANK_CARD);
            }
        } else if (mapBank instanceof DBankAccount) {
            mView.gotoTabLinkAccAndReloadLinkedAcc();
            if (mPayAfterLinkBank) {
                mView.showConfirmPayAfterLinkBank(LinkBankType.LINK_BANK_ACCOUNT);
            }
        }
    }

    @Override
    void onLoadIconFontSuccess() {
        if (mView != null) {
            mView.refreshLinkedCard();
        }
    }

    @Override
    void onDownloadPaymentSDKComplete() {
        if (mView != null) {
            mView.refreshBanksSupport();
        }
    }

    @Override
    public void onErrorLinkCardButInputBankAccount(DBaseMap bankInfo) {
        super.onErrorLinkCardButInputBankAccount(bankInfo);
        if (bankInfo == null) {
            return;
        }
        Timber.d("Start LinkAccount with bank code [%s]", bankInfo.bankcode);
        List<BankAccount> bankAccounts = getLinkedBankAccount();
        if (checkLinkedBankAccount(bankAccounts, bankInfo.bankcode)) {
            String bankName = BankUtils.getBankName(bankInfo.bankcode);
            String message;
            if (!TextUtils.isEmpty(bankName)) {
                message = String.format(getString(R.string.bank_account_has_linked),
                        bankName);
            } else {
                message = getString(R.string.bank_account_has_linked_this_bank);
            }
            if (mView != null) {
                mView.gotoTabLinkAccAndShowDialog(message);
            }
        } else {
            linkAccount(bankInfo.bankcode);
        }
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

    private void showErrorView(int msgResource) {
        if (mView == null || mView.getContext() == null) {
            return;
        }
        mView.hideLoading();
        mView.showError(getContext().getString(msgResource));
    }

    private void showNotificationDialog(int msgResource) {
        if (mView == null || mView.getContext() == null) {
            return;
        }
        mView.hideLoading();
        mView.showNotificationDialog(getContext().getString(msgResource));
    }

    @Override
    void showNetworkErrorDialog() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
        mView.showNetworkErrorDialog();
    }

    @Override
    void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener) {
        if (mView == null) {
            return;
        }
        mView.showRetryDialog(message, listener);
    }

    @Override
    void onGetCardSupportSuccess(List<ZPCard> cardSupportList) {
        Timber.d("on Get Card Support Success");
        if (cardSupportList == null || cardSupportList.size() <= 0) {
            return;
        }
        ArrayList<ZPCard> cards = new ArrayList<>();
        for (ZPCard card : cardSupportList) {
            if (card == null || card.isBankAccount()) {
                continue;
            }
            cards.add(card);
        }
        if (Lists.isEmptyOrNull(cards)) {
            showErrorView(R.string.link_card_bank_support_empty);
        } else {
            if (mView != null) {
                mView.showListBankSupportDialog(cards);
            }
        }
    }

    private String getString(@StringRes int stringResource) {
        if (mView == null || mView.getContext() == null) {
            return "";
        }
        return mView.getContext().getString(stringResource);
    }
}
