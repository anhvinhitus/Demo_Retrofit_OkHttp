package vn.com.zalopay.wallet.business.entity.error;

import vn.com.zalopay.wallet.constants.PaymentError;

public class SdkError {
    @PaymentError
    public int payError;
    public String messError;

    public SdkError(@PaymentError int pErrorCode, String pErrorMessage) {
        this.payError = pErrorCode;
        this.messError = pErrorMessage;
    }
}
