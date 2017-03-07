package vn.com.zalopay.wallet.business.entity.enumeration;

public enum EPaymentReturnCode {

    EXCEPTION(0),
    ATM_VERIFY_OTP_SUCCESS(13),
    ATM_RETRY_CAPTCHA(16),
    ATM_RETRY_OTP(17),
    ATM_CAPTCHA_INVALID(-50);

    private byte mValue = 3;

    private EPaymentReturnCode(int pNum) {
        this.mValue = (byte) pNum;
    }

    public static EPaymentReturnCode fromInt(int pNum) {
        byte num = (byte) pNum;

        for (EPaymentReturnCode status : EPaymentReturnCode.values()) {
            if (status.mValue == num)
                return status;
        }
        return null;
    }

    public int getValue() {
        return mValue;
    }
}
