package vn.com.zalopay.wallet.utils;

import android.text.TextUtils;

public class PaymentUtils {
    /***
     * clear duplicate whitespace
     *
     * @param pCardName
     * @return
     */
    public static String clearCardName(String pCardName) {
        if (!TextUtils.isEmpty(pCardName)) {
            return pCardName.replaceAll("\\s+", " ");
        }
        return pCardName;
    }

    public static String clearOTP(String pOtp) {
        if (!TextUtils.isEmpty(pOtp)) {
            pOtp = pOtp.replace(" ", "");
            pOtp = pOtp.replace("-", "");
        }
        return pOtp;
    }
}
