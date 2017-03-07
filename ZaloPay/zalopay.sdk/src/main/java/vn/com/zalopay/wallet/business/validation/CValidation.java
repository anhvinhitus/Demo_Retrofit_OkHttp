package vn.com.zalopay.wallet.business.validation;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;

public class CValidation implements IPaymentValidate {
    protected IPaymentValidate mPaymentValidattion;

    public CValidation() {
        if (GlobalData.isLinkAccChannel()) {
            setValidator(new CLinkAccValidation());
        } else if (GlobalData.isLinkCardChannel()) {
            setValidator(new CLinkCardValidation());
        } else {
            setValidator(new CPaymentValidation());
        }
    }

    public void setValidator(IPaymentValidate paymentValidate) {
        this.mPaymentValidattion = paymentValidate;
    }

    @Override
    public String onValidateUser() {
        return mPaymentValidattion.onValidateUser();
    }

    @Override
    public String onValidateOrderInfo(ZPWPaymentInfo pParams) {
        return mPaymentValidattion.onValidateOrderInfo(pParams);
    }
}
