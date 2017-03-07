package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;

public class DMappedCard extends DBaseMap {
    public String cardname;
    public String first6cardno;
    public String last4cardno;
    public long expiretime;

    public DMappedCard() {
        bankcode = "";
    }

    /***
     * convert DPaymentCard to DMappedCard
     *
     * @param pCard
     */
    public DMappedCard(DPaymentCard pCard) {
        this.expiretime = 0;

        this.bankcode = pCard.getBankcode();

        this.cardname = pCard.getCardholdername();

        if (!TextUtils.isEmpty(pCard.getCardnumber())) {
            this.first6cardno = pCard.getCardnumber().substring(0, 6);
            this.last4cardno = pCard.getCardnumber().substring(pCard.getCardnumber().length() - 4);
        }
    }

    public DMappedCard clone() {
        DMappedCard mappedCard = new DMappedCard();

        mappedCard.cardname = this.cardname;
        mappedCard.first6cardno = this.first6cardno;
        mappedCard.last4cardno = this.last4cardno;
        mappedCard.bankcode = this.bankcode;
        mappedCard.expiretime = this.expiretime;

        return mappedCard;
    }

    @Override
    public String getFirstNumber() {
        return first6cardno;
    }

    @Override
    public void setFirstNumber(String pFirstNumber) {
        this.first6cardno = pFirstNumber;
    }

    @Override
    public String getLastNumber() {
        return last4cardno;
    }

    @Override
    public void setLastNumber(String pLastNumber) {
        this.last4cardno = pLastNumber;
    }

    @Override
    public boolean isValid() {
        return (!TextUtils.isEmpty(getFirstNumber()) && !TextUtils.isEmpty(getLastNumber()) && (getFirstNumber().length() == 6) && (getLastNumber().length() == 4));
    }

    @Override
    public String getCardKey() {
        return first6cardno + last4cardno;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof DMappedCard)) {
            return false;
        }

        DMappedCard other = (DMappedCard) obj;

        return !TextUtils.isEmpty(this.getFirstNumber()) && !TextUtils.isEmpty(this.getLastNumber()) &&
                this.getFirstNumber().equals(other.first6cardno) && this.getLastNumber().equals(other.last4cardno);
    }
}
