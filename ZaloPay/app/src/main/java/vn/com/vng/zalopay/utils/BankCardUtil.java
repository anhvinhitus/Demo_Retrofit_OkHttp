package vn.com.vng.zalopay.utils;

import android.text.TextUtils;

/**
 * Created by longlv on 27/06/2016.
 */
public class BankCardUtil {

    public static String formatBankCardNumber(String first6cardno, String last4cardno) {
        if (TextUtils.isEmpty(first6cardno) || TextUtils.isEmpty(last4cardno)) {
            return "";
        }
        String bankCardNumber = String.format("%s******%s", first6cardno, last4cardno);
        bankCardNumber = bankCardNumber.replaceAll("(.{4})(?=.)", "$1 ");
        return bankCardNumber;
    }

    public static String formatBankCardNumber(String bankCardNumber) {
        if (TextUtils.isEmpty(bankCardNumber)) {
            return "";
        }
        return bankCardNumber.replaceAll("(.{4})(?=.)", "$1 ");
    }
}
