package vn.com.zalopay.wallet.view.adapter;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.constants.CardType;

/***
 * link card channel show both cc and atm support list
 */
public class LinkCardBankGridViewAdapter extends BankSupportGridViewAdapter {

    public LinkCardBankGridViewAdapter() {
        super();

        String bankCodeVisa = CardType.VISA;
        String bankCodeMaster = CardType.MASTER;
        if (!TextUtils.isEmpty(bankCodeMaster))
            mBankCode.add(bankCodeMaster);
        if (!TextUtils.isEmpty(bankCodeVisa))
            mBankCode.add(bankCodeVisa);
    }
}
