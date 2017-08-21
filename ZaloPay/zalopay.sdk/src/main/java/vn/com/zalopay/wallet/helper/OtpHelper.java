package vn.com.zalopay.wallet.helper;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.PaymentUtils;
import vn.com.zalopay.wallet.entity.config.OtpRule;

/**
 * Created by chucvv on 8/21/17.
 */

public class OtpHelper {
    @Nullable
    public static String parseOtp(List<OtpRule> otpRules, String pSender, String pMessage) {
        if (otpRules == null || otpRules.size() <= 0) {
            return null;
        }
        for (OtpRule otpReceiverPattern : otpRules) {
            if (TextUtils.isEmpty(otpReceiverPattern.sender) || !otpReceiverPattern.sender.equalsIgnoreCase(pSender)) {
                continue;
            }
            String otp = OtpHelper.parseOtp(otpReceiverPattern, pSender, pMessage);
            if (TextUtils.isEmpty(otp)) {
                continue;
            }
            //clear whitespace and - character
            otp = PaymentUtils.clearOTP(otp);
            //check it whether length match length of otp in config
            if (!TextUtils.isEmpty(otp) && otp.length() != otpReceiverPattern.length) {
                continue;
            }
            if ((!otpReceiverPattern.isdigit && TextUtils.isDigitsOnly(otp))
                    || (otpReceiverPattern.isdigit && !TextUtils.isDigitsOnly(otp))) {
                continue;
            }
            return otp;
        }
        return null;
    }

    @Nullable
    private static String parseOtp(OtpRule otpPattern, String pSender, String pMessage) {
        try {
            Timber.d("start parse sms [%s : %s]", pSender, pMessage);
            if (otpPattern == null) {
                return null;
            }
            if (TextUtils.isEmpty(pSender)) {
                return null;
            }
            if (TextUtils.isEmpty(pMessage)) {
                return null;
            }
            if (TextUtils.isEmpty(otpPattern.sender) || !otpPattern.sender.equalsIgnoreCase(pSender)) {
                return null;
            }
            pMessage = pMessage.trim();
            int lengthOtp = otpPattern.length;
            int lengthBody = pMessage.length();
            int start = (otpPattern.begin)
                    ? otpPattern.start : (lengthBody - lengthOtp - otpPattern.start);
            //check before and after otp must be a empty space
            int checkPos = start -1;
            if (checkPos > 0 && (checkPos + 1) < lengthBody && !Character.isWhitespace(pMessage.charAt(checkPos))) {
                return null;
            }
            return pMessage.substring(start, start + lengthOtp);
        } catch (Exception e) {
            Timber.d(e);
        }
        return null;
    }
}
