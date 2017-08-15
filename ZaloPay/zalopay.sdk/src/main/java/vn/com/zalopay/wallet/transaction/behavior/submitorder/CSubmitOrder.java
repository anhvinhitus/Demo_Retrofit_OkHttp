package vn.com.zalopay.wallet.transaction.behavior.submitorder;

import vn.com.zalopay.wallet.api.task.SubmitOrderTask;
import vn.com.zalopay.wallet.entity.bank.PaymentCard;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IDoSubmit;

public class CSubmitOrder implements IDoSubmit {
    @Override
    public void doSubmit(int channelId, UserInfo userInfo, PaymentCard card, PaymentInfoHelper paymentInfoHelper) {
        new SubmitOrderTask(channelId, card, paymentInfoHelper).makeRequest();
    }
}
