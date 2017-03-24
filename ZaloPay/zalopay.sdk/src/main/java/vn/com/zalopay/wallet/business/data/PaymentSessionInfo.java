package vn.com.zalopay.wallet.business.data;

import javax.inject.Inject;

import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.controller.SDKApplication;

public class PaymentSessionInfo extends SingletonBase {
    private static PaymentSessionInfo _object;
    @Inject
    protected ZPWPaymentInfo mPaymentInfo;

    public PaymentSessionInfo() {
        super();
        SDKApplication.getPaymentSessionComponent().inject(this);
    }

    public static PaymentSessionInfo shared() {
        if (PaymentSessionInfo._object == null) {
            PaymentSessionInfo._object = new PaymentSessionInfo();
        }
        return PaymentSessionInfo._object;
    }

    public ZPWPaymentInfo getPaymentInfo() {
        return mPaymentInfo;
    }

}
