package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.controller.SDKApplication;

public class BankHelper {

    public static int getMaxCCLinkNumber(String userId) {

        List<MapCard> mapCardList = SDKApplication
                .getApplicationComponent()
                .linkInteractor()
                .getMapCardList(userId);
        if (mapCardList == null || mapCardList.size() <= 0) {
            return 0;
        }
        int num = 0;
        for (BaseMap card : mapCardList) {
            if (card != null && BankHelper.isInternationalBank(card.bankcode)) {
                num++;
            }
        }
        return num;
    }

    public static boolean hasBankAccountOnCache(String pUserId, String pBankCode) {
        try {
            List<BankAccount> bankAccountList = SDKApplication.getApplicationComponent()
                    .linkInteractor()
                    .getBankAccountList(pUserId);
            BankAccount bankAccount = new BankAccount();
            bankAccount.bankcode = pBankCode;
            return bankAccountList != null && bankAccountList.size() > 0 && bankAccountList.contains(bankAccount);

        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

    public static boolean isBankAccount(String pBankCode) {
        return CardType.PVCB.equals(pBankCode);
    }

    public static boolean isInternationalBank(@CardType String pBankCode) {
        if (TextUtils.isEmpty(pBankCode)) {
            return false;
        }
        if (pBankCode.equals(BuildConfig.CC_CODE)) {
            return true;
        }
        switch (pBankCode) {
            case CardType.VISA:
            case CardType.MASTER:
            case CardType.JCB:
                return true;
            default:
                return false;
        }
    }
}
