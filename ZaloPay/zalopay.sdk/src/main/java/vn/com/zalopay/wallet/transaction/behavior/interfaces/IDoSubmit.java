package vn.com.zalopay.wallet.transaction.behavior.interfaces;

import vn.com.zalopay.wallet.entity.bank.PaymentCard;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

public interface IDoSubmit {
    void doSubmit(int channelId, UserInfo userInfo, PaymentCard card, PaymentInfoHelper paymentInfoHelper);
}
