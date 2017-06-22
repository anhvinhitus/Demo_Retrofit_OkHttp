package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.controller.SDKApplication;

/**
 * Created by longlv on 04/09/2016.
 * Contain valid condition function
 */
public abstract class AbsWithdrawConditionPresenter<View> extends AbstractPresenter<View> {

    protected User mUser;

    protected AbsWithdrawConditionPresenter(User user) {
        this.mUser = user;
    }

    public abstract Activity getActivity();

    protected void validLinkCard(final IListenerValid listenerValid) {
        Timber.d("start get bank support");
        Subscription subscription = SDKApplication
                .getApplicationComponent()
                .bankListInteractor()
                .getWithdrawBanks(vn.com.vng.zalopay.BuildConfig.VERSION_NAME, System.currentTimeMillis())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<List<BankConfig>>() {
                    @Override
                    public void onError(Throwable e) {
                        Timber.d("validLinkCard onError %s", e);
                        String message = ErrorMessageFactory.create(getActivity().getApplicationContext(), e);
                        if (listenerValid != null) {
                            listenerValid.onError(message);
                        }
                    }

                    @Override
                    public void onNext(List<BankConfig> bankConfigs) {
                        Timber.d("validLinkCard onComplete");
                        if (listenerValid == null) {
                            return;
                        }
                        listenerValid.onSuccess(bankConfigs, validLinkCard(bankConfigs), validLinkAccount(bankConfigs));
                    }
                });

        mSubscription.add(subscription);
    }

    private boolean validLinkCard(List<BankConfig> bankConfigs) {
        User user = mUser;
        try {
            if (bankConfigs == null || bankConfigs.isEmpty()) {
                return false;
            }
            List<MapCard> mappedCardList = CShareDataWrapper.getMappedCardList(user);
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
            List<BankAccount> mappedAccounts = CShareDataWrapper.getMapBankAccountList(user);
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

    private boolean existInMappedCard(List<MapCard> userCardList, String bankCode) {
        if (Lists.isEmptyOrNull(userCardList) || TextUtils.isEmpty(bankCode)) {
            return false;
        }
        for (int j = 0; j < userCardList.size(); j++) {
            MapCard mappedCard = userCardList.get(j);
            if (mappedCard == null) {
                continue;
            }
            if (bankCode.equals(mappedCard.bankcode)) {
                return true;
            }
        }
        return false;
    }

    private boolean existInMappedAccount(List<BankAccount> bankAccounts, String bankCode) {
        if (Lists.isEmptyOrNull(bankAccounts) || TextUtils.isEmpty(bankCode)) {
            return false;
        }
        for (int j = 0; j < bankAccounts.size(); j++) {
            BankAccount bankAccount = bankAccounts.get(j);
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
