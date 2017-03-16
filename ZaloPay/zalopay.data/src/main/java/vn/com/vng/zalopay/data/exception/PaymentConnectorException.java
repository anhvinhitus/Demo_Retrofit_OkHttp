package vn.com.vng.zalopay.data.exception;

import android.support.annotation.StringRes;

/**
 * Created by hieuvm on 3/15/17.
 * Exception for payment connector
 * 
 */

public class PaymentConnectorException extends GenericException {

    public PaymentConnectorException(@StringRes int message) {
        super(message);
    }
}
