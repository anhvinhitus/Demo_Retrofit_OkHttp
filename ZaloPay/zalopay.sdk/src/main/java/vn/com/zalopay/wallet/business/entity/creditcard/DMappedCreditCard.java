package vn.com.zalopay.wallet.business.entity.creditcard;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.entity.base.BaseEntity;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

public class DMappedCreditCard extends BaseEntity<DMappedCreditCard> {
    public String f6no;
    public String l4no;
    public String bankcode;

    public DMappedCreditCard(DMappedCard pMappedCard) {
        this.f6no = pMappedCard.first6cardno;
        this.l4no = pMappedCard.last4cardno;
        this.bankcode = pMappedCard.bankcode;
    }

    public boolean isValid() {
        return (!TextUtils.isEmpty(f6no) && !TextUtils.isEmpty(l4no) && (f6no.length() == 6) && (l4no.length() == 4));
    }
}
