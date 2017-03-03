package vn.com.zalopay.wallet.business.entity.error;

import vn.com.zalopay.wallet.business.entity.enumeration.EPayError;

/**
 * Created by admin on 8/9/16.
 */
public class CError {
    public EPayError payError;
    public String messError;

    public CError(EPayError pErrorCode, String pErrorMessage) {
        this.payError = pErrorCode;
        this.messError = pErrorMessage;
    }
}
