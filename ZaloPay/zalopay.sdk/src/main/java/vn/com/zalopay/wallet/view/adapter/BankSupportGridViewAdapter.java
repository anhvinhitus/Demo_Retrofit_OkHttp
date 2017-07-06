package vn.com.zalopay.wallet.view.adapter;

import java.util.Map;

import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.helper.BankAccountHelper;

/***
 * list bank support.
 */
public class BankSupportGridViewAdapter extends CardSupportAdapter {
    public BankSupportGridViewAdapter() {
        Map<String, String> bankPrefix = SDKApplication.getApplicationComponent()
                .bankListInteractor()
                .getBankPrefix();
        if (bankPrefix != null) {
            for (Object o : bankPrefix.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                String bankCode = String.valueOf(pair.getValue());
                //if( !mBankCode.contains(String.valueOf(pair.getValue())) && !isBankMaintenance(String.valueOf(pair.getValue())))
                if (!mBankCode.contains(bankCode)) {
                    mBankCode.add(String.valueOf(pair.getValue()));
                }
            }
        }
    }

    public CardSupportAdapter getAdapter() {
        return new BankSupportGridViewAdapter();
    }
}