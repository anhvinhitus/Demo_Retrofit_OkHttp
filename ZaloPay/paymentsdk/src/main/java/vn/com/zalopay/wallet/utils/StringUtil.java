package vn.com.zalopay.wallet.utils;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankFunction;

public class StringUtil {

    public static String longToStringNoDecimal(long pLongNumber) {
        return NumberFormat.getNumberInstance(Locale.US).format(pLongNumber);
    }

    public static String getFormattedBankMaintenaceMessage() {
        String message = "Ngân hàng đang bảo trì.Vui lòng quay lại sau ít phút.";
        try {
            String maintenanceTo = "";

            BankConfig bankConfig = BankLoader.getInstance().maintenanceBank;
            BankFunction bankFunction = BankLoader.getInstance().maintenanceBankFunction;

            if (bankConfig != null && bankConfig.isBankFunctionAllMaintenance()) {
                message = bankConfig.maintenancemsg;

                if (bankConfig.maintenanceto > 0) {
                    maintenanceTo = ZPWUtils.convertDateTime(bankConfig.maintenanceto);
                }

                if (!TextUtils.isEmpty(message) && message.contains("%s")) {
                    message = String.format(message, maintenanceTo);
                } else if (TextUtils.isEmpty(message)) {
                    message = GlobalData.getStringResource(RS.string.zpw_string_bank_maitenance);
                    message = String.format(message, bankConfig.name, maintenanceTo);
                }

            }
            if (bankConfig != null && bankFunction != null && bankFunction.isFunctionMaintenance()) {
                maintenanceTo = ZPWUtils.convertDateTime(bankFunction.maintenanceto);
                message = GlobalData.getStringResource(RS.string.zpw_string_bank_maitenance);
                message = String.format(message, bankConfig.name, maintenanceTo);
            }
        } catch (Exception ex) {
            Log.e("getFormattedBankMaintenaceMessage", ex);
        }
        return message;
    }

    public static String formatVnCurrence(String price) {

        NumberFormat format = new DecimalFormat("#,##0.00");

        format.setCurrency(Currency.getInstance(Locale.US));//Or default locale

        price = (!TextUtils.isEmpty(price)) ? price : "0";

        price = price.trim();
        price = format.format(Math.ceil(Double.parseDouble(price)));

        price = price.replaceAll(",", "\\.");

        if (price.endsWith(".00")) {
            int centsIndex = price.lastIndexOf(".00");
            if (centsIndex != -1) {
                price = price.substring(0, centsIndex);
            }
        }

        price = String.format("%s", price);

        return price;
    }

    public static String getFirstStringWithSize(String pString, int pSize) {
        return pString.substring(0, pSize);
    }

    public static String getLastStringWithSize(String pString, int pSize) {
        return pString.substring(pString.length() - pSize);
    }
}
