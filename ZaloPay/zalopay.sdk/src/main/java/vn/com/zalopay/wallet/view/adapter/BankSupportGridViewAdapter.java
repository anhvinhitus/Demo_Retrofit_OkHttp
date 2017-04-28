package vn.com.zalopay.wallet.view.adapter;

import java.util.Map;

import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;

/***
 * list bank support.
 */
public class BankSupportGridViewAdapter extends CardSupportAdapter {
    public BankSupportGridViewAdapter() {
        if (BankLoader.mapBank != null) {

            for (Object o : BankLoader.mapBank.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
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