package vn.com.zalopay.wallet.business.fingerprint;

public class PaymentFingerPrint extends BaseFingerPrint {
    protected static PaymentFingerPrint _object;

    public PaymentFingerPrint() {
        super();
    }

    public static PaymentFingerPrint shared() {
        if (PaymentFingerPrint._object == null) {
            PaymentFingerPrint._object = new PaymentFingerPrint();
        }
        return PaymentFingerPrint._object;
    }

}
