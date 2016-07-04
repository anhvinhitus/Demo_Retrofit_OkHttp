package vn.com.vng.zalopay.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.R;

/**
 * Created by longlv on 27/06/2016.
 */
public class BankCardUtil {

    public static List<Integer> PARTICIPATE_BANK_ICONS;

    static {
        PARTICIPATE_BANK_ICONS = new ArrayList<>();
        PARTICIPATE_BANK_ICONS.add(R.drawable.ic_bidv_large);
        PARTICIPATE_BANK_ICONS.add(R.drawable.ic_eximbank_large);
        PARTICIPATE_BANK_ICONS.add(R.drawable.ic_agribank_large);
        PARTICIPATE_BANK_ICONS.add(R.drawable.ic_sacombank_large);
        PARTICIPATE_BANK_ICONS.add(R.drawable.ic_vietcombank_large);
        PARTICIPATE_BANK_ICONS.add(R.drawable.ic_vietinbank_large);
        PARTICIPATE_BANK_ICONS.add(R.drawable.ic_visa_large);
        PARTICIPATE_BANK_ICONS.add(R.drawable.ic_mastercard_large);
        PARTICIPATE_BANK_ICONS.add(R.drawable.ic_jcb_large);
    }

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
