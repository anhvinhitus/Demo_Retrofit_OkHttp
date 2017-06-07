package vn.com.vng.zalopay.bank.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import rx.Subscription;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.LinkBankType;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by longlv on 1/16/17.
 * Presenter of BankSupportFragment.
 */

class BankSupportPresenter extends AbstractPresenter<IBankSupportView> {

    private final User mUser;
    private LinkBankType mBankType;
    private DefaultSubscriber<List<ZPCard>> mGetSupportBankSubscriber;

    @Inject
    BankSupportPresenter(User user) {
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
                refreshListBank(cardList);
            }
        };
    }

    void getCardSupport() {
        Timber.d("Get list bank support %s", mBankType);
        UserInfo userInfo = new UserInfo();
        userInfo.zalopay_userid = mUser.zaloPayId;
        userInfo.accesstoken = mUser.accesstoken;
        Subscription subscription = CShareDataWrapper.getCardSupportList(userInfo, mGetSupportBankSubscriber);
        mSubscription.add(subscription);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private void showRetryDialog() {
        if (mView == null || mView.getContext() == null) {
            return;
        }

        mView.showRetryDialog(mView.getContext().getString(R.string.exception_generic), new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {

            }

            @Override
            public void onOKevent() {
                getCardSupport();
            }
        });
    }

    public void showLoading() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    public void hideLoading() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    private void refreshListBank(List<ZPCard> listBank) {
        if (mBankType == LinkBankType.LINK_BANK_CARD) {
            setTitleListBank(R.string.link_card_title);
            setListBank(listBank, LinkBankType.LINK_BANK_CARD);
        } else {
            setTitleListBank(R.string.link_account_title);
            setListBank(listBank, LinkBankType.LINK_BANK_ACCOUNT);
        }
    }

    private void setTitleListBank(@StringRes int strResource) {
        if (mView != null) {
            mView.setTitleListBank(strResource);
        }
    }

    private void setListBank(List<ZPCard> listBank, LinkBankType bankType) {
        if (mView != null) {
            mView.refreshListBank(filterListBank(listBank, bankType));
        }
    }

    private List<ZPCard> filterListBank(List<ZPCard> cardList, LinkBankType bankType) {
        if (Lists.isEmptyOrNull(cardList)) {
            return Collections.emptyList();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return cardList.stream().filter(card -> isValidLinkBankType(card, bankType))
                    .collect(Collectors.toList());
        } else {
            List<ZPCard> listBank = new ArrayList<>();
            for (ZPCard card : cardList) {
                if (isValidLinkBankType(card, bankType)) {
                    listBank.add(card);
                }
            }
            return listBank;
        }
    }

    private boolean isValidLinkBankType(ZPCard card, LinkBankType bankType) {
        return (card != null
                && (bankType == LinkBankType.LINK_BANK_ACCOUNT && card.isBankAccount()
                || bankType == LinkBankType.LINK_BANK_CARD && !card.isBankAccount()));
    }

    void iniData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mBankType = (LinkBankType) bundle.getSerializable(Constants.ARG_LINK_BANK_TYPE);
    }
}
