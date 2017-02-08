package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.merchant.listener.IGetWithDrawBankList;

/**
 * Created by longlv on 04/09/2016.
 * Contain valid condition function
 */
public abstract class AbsWithdrawConditionPresenter<View> extends AbstractPresenter<View> {

    public abstract Activity getActivity();

    protected User mUser;

    protected AbsWithdrawConditionPresenter(User user) {
        this.mUser = user;
    }

    protected boolean isValidProfile() {
        User user = mUser;
        return !(user == null || user.profilelevel < 2);
    }

    protected void validLinkCard(final IListenerValid listenerValid) {
        CShareDataWrapper.getWithDrawBankList(new IGetWithDrawBankList() {
                    @Override
                    public void onComplete(List<BankConfig> list) {
                        Timber.d("validLinkCard onComplete");
                        if (listenerValid == null) {
                            return;
                        }
                        listenerValid.onSuccess(list, validLinkCard(list), validLinkAccount(list));
                    }

                    @Override
                    public void onError(String error) {
                        Timber.d("validLinkCard onError");
                        if (listenerValid != null) {
                            listenerValid.onError(error);
                        }
                    }
                });
    }

    private boolean validLinkCard(List<BankConfig> bankConfigs) {
        User user = mUser;
        try {
            if (bankConfigs == null || bankConfigs.isEmpty()) {
                return false;
            }
            List<DMappedCard> mappedCardList = CShareDataWrapper.getMappedCardList(user.zaloPayId);
            for (int j = 0; j < bankConfigs.size(); j++) {
                BankConfig bankConfig = bankConfigs.get(j);
                if (bankConfig == null || bankConfig.isBankAccount()) {
                    continue;
                }
                if (existInMappedCard(mappedCardList, bankConfig.code)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Timber.w(e, "Get mapped card exception: %s", e.getMessage());
        }
        return false;
    }

    private boolean validLinkAccount(List<BankConfig> bankConfigs) {
        User user = mUser;
        try {
            if (bankConfigs == null || bankConfigs.isEmpty()) {
                return false;
            }
            List<DBankAccount> mappedAccounts = CShareDataWrapper.getMapBankAccountList(user.zaloPayId);
            for (int j = 0; j < bankConfigs.size(); j++) {
                BankConfig bankConfig = bankConfigs.get(j);
                if (bankConfig == null || !bankConfig.isBankAccount()) {
                    continue;
                }
                if (existInMappedAccount(mappedAccounts, bankConfig.code)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Timber.w(e, "Get mapped card exception: %s", e.getMessage());
        }
        return false;
    }

    private boolean existInMappedCard(List<DMappedCard> userCardList, String bankCode) {
        if (Lists.isEmptyOrNull(userCardList) || TextUtils.isEmpty(bankCode)) {
            return false;
        }
        for (int j = 0; j < userCardList.size(); j++) {
            DMappedCard mappedCard = userCardList.get(j);
            if (mappedCard == null) {
                continue;
            }
            if (bankCode.equals(mappedCard.bankcode)) {
                return true;
            }
        }
        return false;
    }

    private boolean existInMappedAccount(List<DBankAccount> bankAccounts, String bankCode) {
        if (Lists.isEmptyOrNull(bankAccounts) || TextUtils.isEmpty(bankCode)) {
            return false;
        }
        for (int j = 0; j < bankAccounts.size(); j++) {
            DBankAccount bankAccount = bankAccounts.get(j);
            if (bankAccount == null) {
                continue;
            }
            if (bankCode.equals(bankAccount.bankcode)) {
                return true;
            }
        }
        return false;
    }

    public interface IListenerValid {
        void onSuccess(List<BankConfig> list, boolean isValidLinkCard, boolean isValidLinkAccount);

        void onError(String error);
    }
}
