package vn.com.zalopay.wallet.merchant.strategy;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.listener.IDetectCardTypeListener;
import vn.com.zalopay.wallet.utils.Log;

public class TaskDetectCardType extends TaskBase {
    protected String mCardNumber;

    public TaskDetectCardType(String pCardNumber) {
        super();
        this.mCardNumber = pCardNumber;
    }

    @Override
    protected void onDoIt() {
        if (TextUtils.isEmpty(mCardNumber) && getListener() instanceof IDetectCardTypeListener) {
            ((IDetectCardTypeListener) getListener()).onComplete(ECardType.UNDEFINE);

            Log.d(this, "===mCardNumber=NULL");
            return;
        }

        if (CShareData.loadConfigBundle() != null) {
            CreditCardCheck cardCheck = new CreditCardCheck(CShareData.getConfigResource().CCIdentifier);
            cardCheck.detectCard(mCardNumber);

            ECardType eCardType = ECardType.fromString(cardCheck.getCodeBankForVerify());

            if (getListener() instanceof IDetectCardTypeListener) {
                ((IDetectCardTypeListener) getListener()).onComplete(eCardType);
            }
        } else {
            Log.d(this, "===CShareData.getConfigResource()=NULL===");
            getListener().onError(null);
        }
    }
}
