package vn.com.zalopay.wallet.business.validation;

import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.paymentinfo.IPaymentInfo;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

public class PaymentInfoValidation implements IValidate {
    protected IValidate validate;

    public PaymentInfoValidation(PaymentInfoHelper paymentInfoHelper) {
        if (paymentInfoHelper.isBankAccountTrans()) {
            setValidator(new LinkAccValidation());
        } else if (paymentInfoHelper.isLinkTrans()) {
            setValidator(new LinkCardValidation());
        } else {
            setValidator(new PayValidation());
        }
    }

    public void setValidator(IValidate paymentValidate) {
        this.validate = paymentValidate;
    }

    @Override
    public String onValidateUser(UserInfo userInfo) {
        return validate.onValidateUser(userInfo);
    }

    @Override
    public String onValidateOrder(AbstractOrder order) {
        return validate.onValidateOrder(order);
    }

    @Override
    public String onValidate(IPaymentInfo paymentInfo) {
        return validate.onValidate(paymentInfo);
    }
}
