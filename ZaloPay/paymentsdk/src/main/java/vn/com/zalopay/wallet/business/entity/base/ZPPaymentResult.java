package vn.com.zalopay.wallet.business.entity.base;

import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentStatus;

/***
 * result payment callback to app
 */
public class ZPPaymentResult {
    //payment info app tranfer to sdk
    public ZPWPaymentInfo paymentInfo;
    //payment status: success,fail,processing...
    public EPaymentStatus paymentStatus;

    public String channelID;
    public String channelDetail;

    //user pay success,sdk auto map card,return mapped card to app to show linkcard tutorial page
    public DMapCardResult mapCardResult;

    public ZPPaymentResult(ZPWPaymentInfo pPaymentInfo, EPaymentStatus pPaymentStatus) {
        this.paymentInfo = pPaymentInfo;
        this.paymentStatus = pPaymentStatus;
    }
}
