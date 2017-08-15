package vn.com.zalopay.wallet.entity.bank;

import android.text.TextUtils;

import vn.com.zalopay.wallet.entity.bank.MapCard;

public class CardSubmit {
    public String f6no;
    public String l4no;
    public String bankcode;
    public String cardhash;

    public CardSubmit(MapCard mapCard) {
        this.f6no = mapCard.first6cardno;
        this.l4no = mapCard.last4cardno;
        this.bankcode = mapCard.bankcode;
        this.cardhash = mapCard.cardhash;
    }

    public boolean isValid() {
        return (!TextUtils.isEmpty(f6no) && !TextUtils.isEmpty(l4no) && (f6no.length() == 6) && (l4no.length() == 4));
    }
}
