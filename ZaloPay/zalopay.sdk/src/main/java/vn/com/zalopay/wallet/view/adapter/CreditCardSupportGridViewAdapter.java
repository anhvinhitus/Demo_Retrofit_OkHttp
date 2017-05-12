package vn.com.zalopay.wallet.view.adapter;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.constants.CardType;

/***
 * list credit card support.
 */
public class CreditCardSupportGridViewAdapter extends CardSupportAdapter {
    public CreditCardSupportGridViewAdapter() {
        String bankCodeVisa = CardType.VISA;
        String bankCodeMaster = CardType.MASTER;

        if (!TextUtils.isEmpty(bankCodeVisa))
            mBankCode.add(bankCodeVisa);
        if (!TextUtils.isEmpty(bankCodeMaster))
            mBankCode.add(bankCodeMaster);
    }
}