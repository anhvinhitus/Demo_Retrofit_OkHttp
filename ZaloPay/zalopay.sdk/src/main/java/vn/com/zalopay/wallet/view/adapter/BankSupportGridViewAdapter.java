package vn.com.zalopay.wallet.view.adapter;

import java.util.Iterator;
import java.util.Map;

import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;

/***
 * list bank support.
 */
public class BankSupportGridViewAdapter extends CardSupportAdapter {
    public BankSupportGridViewAdapter() {
        if (BankLoader.mapBank != null) {
            Iterator it = BankLoader.mapBank.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                //if( !mBankCode.contains(String.valueOf(pair.getValue())) && !isBankMaintenance(String.valueOf(pair.getValue())))
                if (!mBankCode.contains(String.valueOf(pair.getValue()))) {
                    mBankCode.add(String.valueOf(pair.getValue()));
                }
            }
        }
    }

    public CardSupportAdapter getAdapter() {
        return new BankSupportGridViewAdapter();
    }
}