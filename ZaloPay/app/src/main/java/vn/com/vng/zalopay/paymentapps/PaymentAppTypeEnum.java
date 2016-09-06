package vn.com.vng.zalopay.paymentapps;

/**
 * Created by longlv on 05/09/2016.
 * Application type of app in home page
 */
public enum PaymentAppTypeEnum {
    NATIVE(1), WEBVIEW(2);
    int value;

    PaymentAppTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
