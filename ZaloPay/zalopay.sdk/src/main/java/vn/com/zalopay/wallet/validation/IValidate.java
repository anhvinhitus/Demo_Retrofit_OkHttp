package vn.com.zalopay.wallet.validation;

import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.IPaymentInfo;

public interface IValidate {
    String onValidateUser(UserInfo userInfo);

    String onValidateOrder(AbstractOrder order);

    String onValidate(IPaymentInfo paymentInfo);
}
