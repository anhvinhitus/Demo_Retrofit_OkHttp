package vn.com.zalopay.wallet.business.transaction;

import vn.com.zalopay.wallet.business.transaction.behavior.authenpayer.CAuthenPayerMapCard;
import vn.com.zalopay.wallet.business.transaction.behavior.base.BasePaymentTransaction;
import vn.com.zalopay.wallet.business.transaction.behavior.getstatus.CGetMapCardStatus;
import vn.com.zalopay.wallet.business.transaction.behavior.submitorder.CVerifyCardMap;


public class SDKMapCard extends BasePaymentTransaction {
    private static SDKMapCard _object;

    public SDKMapCard() {
        super();

        setDoSubmitInterface(new CVerifyCardMap());
        setGetStatusInterface(new CGetMapCardStatus());
        setAuthenPayerInferface(new CAuthenPayerMapCard());
    }

    public static SDKMapCard shared() {
        if (SDKMapCard._object == null) {
            SDKMapCard._object = new SDKMapCard();
        }
        return SDKMapCard._object;
    }
}
