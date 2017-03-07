package vn.com.zalopay.wallet.business.transaction;


import vn.com.zalopay.wallet.business.transaction.behavior.authenpayer.CAuthenPayer;
import vn.com.zalopay.wallet.business.transaction.behavior.base.BasePaymentTransaction;
import vn.com.zalopay.wallet.business.transaction.behavior.getstatus.CGetPaymentStatus;
import vn.com.zalopay.wallet.business.transaction.behavior.submitorder.CSubmitOrder;

public class SDKPayment extends BasePaymentTransaction {
    private static SDKPayment _object;

    public SDKPayment() {
        super();

        setDoSubmitInterface(new CSubmitOrder());
        setGetStatusInterface(new CGetPaymentStatus());
        setAuthenPayerInferface(new CAuthenPayer());
    }

    public static SDKPayment shared() {
        if (SDKPayment._object == null) {
            SDKPayment._object = new SDKPayment();
        }
        return SDKPayment._object;
    }
}
