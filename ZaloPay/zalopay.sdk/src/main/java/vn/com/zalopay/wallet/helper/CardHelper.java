package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.card.BankDetector;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.card.CreditCardDetector;
import vn.com.zalopay.wallet.constants.CardTypeUtils;

public class CardHelper {

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
