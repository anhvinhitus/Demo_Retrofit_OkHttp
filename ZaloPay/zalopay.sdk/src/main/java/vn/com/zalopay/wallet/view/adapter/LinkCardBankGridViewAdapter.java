package vn.com.zalopay.wallet.view.adapter;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;

/***
 * link card channel show both cc and atm support list
 */
public class LinkCardBankGridViewAdapter extends BankSupportGridViewAdapter {

    public LinkCardBankGridViewAdapter() {
        super();
        String bankCodeVisa = GlobalData.getStringResource(RS.string.zpw_string_bankcode_visa);
        String bankCodeMaster = GlobalData.getStringResource(RS.string.zpw_string_bankcode_master);
        if (!TextUtils.isEmpty(bankCodeMaster))
            mBankCode.add(bankCodeMaster);
        if (!TextUtils.isEmpty(bankCodeVisa))
            mBankCode.add(bankCodeVisa);
    }
}
