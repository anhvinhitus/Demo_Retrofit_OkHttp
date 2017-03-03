package vn.com.zalopay.wallet.view.adapter;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;

/***
 * list credit card support.
 */
public class CreditCardSupportGridViewAdapter extends CardSupportAdapter {
    public CreditCardSupportGridViewAdapter() {
        String bankCodeVisa = GlobalData.getStringResource(RS.string.zpw_string_bankcode_visa);
        String bankCodeMaster = GlobalData.getStringResource(RS.string.zpw_string_bankcode_master);
        String bankCodeJcb = GlobalData.getStringResource(RS.string.zpw_string_bankcode_jcb);

        if (!TextUtils.isEmpty(bankCodeVisa))
            mBankCode.add(bankCodeVisa);
        if (!TextUtils.isEmpty(bankCodeMaster))
            mBankCode.add(bankCodeMaster);
        if (!TextUtils.isEmpty(bankCodeJcb))
            mBankCode.add(bankCodeJcb);
    }
}