package vn.com.vng.zalopay.ui;

/**
 * Created by longlv on 05/09/2016.
 *
 */
public enum AppTypeEnum {
    NATIVE(1), WEBVIEW(2);
    int value;

    AppTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
