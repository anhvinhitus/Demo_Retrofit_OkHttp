package vn.com.zalopay.wallet.business.entity.base;

import vn.com.zalopay.wallet.constants.PaymentStatus;

/***
 * result payment callback to app
 */
public class ZPPaymentResult {
    public ZPWPaymentInfo paymentInfo;
    @PaymentStatus
    public int paymentStatus;

    public String channelID;
    public String channelDetail;

    //user pay success,sdk auto map card,return mapped card to app to show linkcard tutorial page
    public DMapCardResult mapCardResult;

    public ZPPaymentResult(ZPWPaymentInfo pPaymentInfo, @PaymentStatus int pPaymentStatus) {
        this.paymentInfo = pPaymentInfo;
        this.paymentStatus = pPaymentStatus;
    }
}
