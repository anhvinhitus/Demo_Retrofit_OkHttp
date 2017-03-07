package vn.com.zalopay.wallet.utils;

/**
 * Created by cpu11843-local on 2/17/17.
 */

public class OtpUtils {
    public static String getOtp(String pSms, String pIdentify, String pPrefixOtp, int pLength) {
        String otp = null;
        if (pSms.contains(pIdentify) && pSms.contains(pPrefixOtp)) { // identify sms && check sms have prefixOtp
            int beginIndex = pSms.lastIndexOf(pPrefixOtp) + pPrefixOtp.length();
            otp = pSms.substring(beginIndex, beginIndex + pLength);
        }
        return otp;
    }
}
