package vn.com.zalopay.wallet.business.validation;

import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;

public interface IPaymentValidate {
    String onValidateUser();

    String onValidateOrderInfo(ZPWPaymentInfo pParams);
}
