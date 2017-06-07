package vn.com.zalopay.wallet.business.validation;

import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.paymentinfo.IPaymentInfo;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;

public class PaymentInfoValidation implements IValidate {
    protected IValidate validate;

    public PaymentInfoValidation(@TransactionType int transtype) {
        if (transtype == TransactionType.LINK_ACCOUNT) {
            setValidator(new LinkAccValidation());
        } else if (transtype == TransactionType.LINK_CARD) {
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
