package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.card.BankDetector;
import vn.com.zalopay.wallet.card.CreditCardDetector;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.CardTypeUtils;
import vn.com.zalopay.wallet.controller.SDKApplication;

public class BankHelper {

    public static int getMaxCCLinkNum(String userId) {

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

    public static DMapCardResult cast(MapCard saveCardInfo) {
        DMapCardResult mapCardResult = new DMapCardResult();
        mapCardResult.setLast4Number(saveCardInfo.last4cardno);
        String bankName = null;
        //this is atm card
        if (!BuildConfig.CC_CODE.equals(saveCardInfo.bankcode)) {
            mapCardResult.setCardLogo(ChannelHelper.makeCardIconNameFromBankCode(saveCardInfo.bankcode));
            bankName = BankDetector.getInstance().getShortBankName();
            if (!TextUtils.isEmpty(bankName)) {
                bankName = String.format(GlobalData.getAppContext().getResources().getString(R.string.sdk_card_link_format), bankName);
            }
        }
        //cc
        else {
            CreditCardDetector cardCheck = CreditCardDetector.getInstance();
            cardCheck.detectOnSync(saveCardInfo.first6cardno);
            if (cardCheck.detected()) {
                String cardType = CardTypeUtils.fromBankCode(cardCheck.getCodeBankForVerifyCC());
                mapCardResult.setCardLogo(ChannelHelper.makeCardIconNameFromBankCode(cardType));
                bankName = String.format(GlobalData.getAppContext().getResources().getString(R.string.sdk_card_link_format), cardCheck.getBankName());
            }
        }
        if (TextUtils.isEmpty(bankName)) {
            bankName = GlobalData.getAppContext().getResources().getString(R.string.sdk_card_link_default_format);
        }
        mapCardResult.setBankName(bankName);
        return mapCardResult;
    }
}
