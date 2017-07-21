package vn.com.zalopay.wallet.transaction.behavior.interfaces;

import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

public interface IDoSubmit {
    void doSubmit(int channelId, UserInfo userInfo, DPaymentCard card, PaymentInfoHelper paymentInfoHelper);
}
