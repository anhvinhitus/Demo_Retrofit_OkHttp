package vn.com.vng.zalopay.scanners.qrcode;

/**
 * Created by longlv on 3/20/17.
 * QR Code resource that zalo pay support.
 */

enum QRCodeResource {

    SCANNER(0), PHOTO_LIBRARY(1), HTTP_REQUEST(2);

    int value;

    QRCodeResource(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
