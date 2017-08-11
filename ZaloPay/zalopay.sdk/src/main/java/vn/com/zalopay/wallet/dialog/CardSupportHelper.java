package vn.com.zalopay.wallet.dialog;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Map;

import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.controller.SDKApplication;

/*
 * Created by lytm on 18/07/2017.
 */

public class CardSupportHelper {
    public static ArrayList<String> getLocalBankSupport() {
        ArrayList<String> mListBankCode = new ArrayList<>();
        Map<String, String> bankPrefix = SDKApplication.getApplicationComponent()
                .bankListInteractor()
                .getBankPrefix();
        if (bankPrefix != null) {
            for (Object o : bankPrefix.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                String bankCode = String.valueOf(pair.getValue());
                if (!mListBankCode.contains(bankCode)) {
                    mListBankCode.add(String.valueOf(pair.getValue()));
                }
            }
        }
        return mListBankCode;
    }

    public static ArrayList<String> getLinkCardSupport() {
        ArrayList<String> mListBankCode = getLocalBankSupport();
        String bankCodeVisa = CardType.VISA;
        String bankCodeMaster = CardType.MASTER;
        if (!TextUtils.isEmpty(bankCodeMaster)) {
            mListBankCode.add(bankCodeMaster);
        }
        if (!TextUtils.isEmpty(bankCodeVisa)) {
            mListBankCode.add(bankCodeVisa);
        }
        return mListBankCode;
    }

    public static ArrayList<String> getCardSupport() {
        ArrayList<String> mListBankCode = new ArrayList<>();
        String bankCodeVisa = CardType.VISA;
        String bankCodeMaster = CardType.MASTER;
        if (!TextUtils.isEmpty(bankCodeMaster)) {
            mListBankCode.add(bankCodeMaster);
        }
        if (!TextUtils.isEmpty(bankCodeVisa)) {
            mListBankCode.add(bankCodeVisa);
        }
        return mListBankCode;
    }

}
