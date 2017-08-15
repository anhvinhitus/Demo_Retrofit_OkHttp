package vn.com.zalopay.wallet.transaction.behavior.submitorder;

import vn.com.zalopay.wallet.api.task.VerifyMapCardTask;
import vn.com.zalopay.wallet.business.entity.base.PaymentCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IDoSubmit;

public class CVerifyCardMap implements IDoSubmit {
    @Override
    public void doSubmit(int channelId, UserInfo userInfo, PaymentCard card, PaymentInfoHelper paymentInfoHelper) {
        new VerifyMapCardTask(userInfo, card).makeRequest();
    }
}
