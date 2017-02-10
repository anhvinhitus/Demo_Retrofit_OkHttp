package vn.com.vng.zalopay.webapp;

/**
 * Created by longlv on 2/10/17.
 * *
 */

interface IPaymentListener {
    void onPayError(String param);
    void onPaySuccess();
}
