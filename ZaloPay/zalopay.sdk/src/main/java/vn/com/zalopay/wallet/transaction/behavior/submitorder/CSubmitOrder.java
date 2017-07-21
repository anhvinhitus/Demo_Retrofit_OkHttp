package vn.com.zalopay.wallet.transaction.behavior.submitorder;

import vn.com.zalopay.wallet.api.task.SubmitOrderTask;
import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IDoSubmit;

public class CSubmitOrder implements IDoSubmit {
    @Override
    public void doSubmit(int channelId, UserInfo userInfo, DPaymentCard card, PaymentInfoHelper paymentInfoHelper) {
        new SubmitOrderTask(channelId, card, paymentInfoHelper).makeRequest();
    }
}
