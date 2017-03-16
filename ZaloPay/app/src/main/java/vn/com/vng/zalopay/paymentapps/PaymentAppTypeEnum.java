package vn.com.vng.zalopay.paymentapps;

/**
 * Created by longlv on 05/09/2016.
 * Application type of app in home page
 */
public enum PaymentAppTypeEnum {
    REACT_NATIVE(1), WEBVIEW(2), INTERNAL_APP(-1), INTERNAL_REACT_NATIVE(-2);
    int value;

    PaymentAppTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
