package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.zalopay.wallet.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 04/09/2016.
 * Contain valid condition function
 */
public abstract class AbsWithdrawConditionPresenter extends BaseUserPresenter {

    public abstract Activity getActivity();
    public abstract void setChkEmail(boolean isValid);
    public abstract void setChkIdentityNumber(boolean isValid);
    public abstract void setChkVietinBank(boolean isValid);
    public abstract void setChkSacomBank(boolean isValid);

    protected boolean isValidProfileLevel() {
        User user = userConfig.getCurrentUser();
        if (user == null) {
            return false;
        }
        boolean isValid = true;
        if (!TextUtils.isEmpty(user.email)) {
            setChkEmail(true);
        } else {
            isValid = false;
        }
        if (!TextUtils.isEmpty(user.identityNumber)) {
            setChkIdentityNumber(true);
        } else {
            isValid = false;
        }
        return isValid;
    }

    protected boolean isValidLinkCard() {
        User user = userConfig.getCurrentUser();
        boolean isMapped = false;
        try {
            List<DMappedCard> mapCardLis = CShareData.getInstance(getActivity()).getMappedCardList(user.zaloPayId);
            if (mapCardLis == null || mapCardLis.size() <= 0) {
                return false;
            }
            for (int i = 0; i < mapCardLis.size(); i++) {
                DMappedCard card = mapCardLis.get(i);
                if (card == null || TextUtils.isEmpty(card.bankcode)) {
                    continue;
                }
                if (ECardType.PVTB.toString().equals(card.bankcode)) {
                    setChkVietinBank(true);
                    isMapped = true;
                } else if (ECardType.PSCB.toString().equals(card.bankcode)) {
                    setChkSacomBank(true);
                    isMapped = true;
                }
            }
            return isMapped;
        } catch (Exception e) {
            Timber.w(e, "Get mapped card exception: %s", e.getMessage());
        }
        return isMapped;
    }


}
