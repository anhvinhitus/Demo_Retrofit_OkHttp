package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.listener.IGetWithDrawBankList;

/**
 * Created by longlv on 04/09/2016.
 * Contain valid condition function
 */
public abstract class AbsWithdrawConditionPresenter extends BaseUserPresenter {

    public abstract Activity getActivity();

    protected UserConfig mUserConfig;

    protected AbsWithdrawConditionPresenter(UserConfig userConfig) {
        this.mUserConfig = userConfig;
    }

    protected boolean isValidProfile() {
        User user = mUserConfig.getCurrentUser();
        return !(user == null || user.profilelevel <= 2);
    }

    protected void validLinkCard(final IListenerValid listenerValid) {
        CShareData.getInstance()
                .getWithDrawBankList(new IGetWithDrawBankList() {
                    @Override
                    public void onComplete(List<BankConfig> list) {
                        Timber.d("validLinkCard onComplete");
                        if (listenerValid == null) {
                            return;
                        }
                        listenerValid.onSuccess(list, validLinkCard(list));
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

    private boolean validLinkCard(List<BankConfig> listCardSupportWithdraw) {
        User user = mUserConfig.getCurrentUser();
        try {
            if (listCardSupportWithdraw == null || listCardSupportWithdraw.isEmpty()) {
                return false;
            }
            List<DMappedCard> mappedCardList = CShareData.getInstance().getMappedCardList(user.zaloPayId);
            for (int j = 0; j < listCardSupportWithdraw.size(); j++) {
                BankConfig bankConfig = listCardSupportWithdraw.get(j);
                if (bankConfig == null) {
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

    private boolean existInMappedCard(List<DMappedCard> userCardList, String bankCode) {
        if (userCardList == null || userCardList.size() <= 0) {
            return false;
        }
        if (TextUtils.isEmpty(bankCode)) {
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

    public interface IListenerValid {
        void onSuccess(List<BankConfig> list, boolean isValid);

        void onError(String error);
    }
}
