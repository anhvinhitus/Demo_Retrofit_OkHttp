package vn.com.vng.zalopay.scanners.ui;

import vn.com.vng.zalopay.scanners.controller.PaymentRecord;

/**
 * Created by huuhoa on 6/1/16.
 * Interface for handling NFC content
 */
public interface NfcView {
    int STATUS_NOT_AVAILABLE = 1;
    int STATUS_DISABLE = 2;
    int STATUS_ENABLE = 3;

    void onInitDone(int status);
    void onReceivePaymentRecord(PaymentRecord paymentRecord);
}
