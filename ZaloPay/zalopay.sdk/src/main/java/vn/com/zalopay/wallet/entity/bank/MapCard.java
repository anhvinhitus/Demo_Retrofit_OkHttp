package vn.com.zalopay.wallet.entity.bank;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class MapCard extends BaseMap {

    @SerializedName("cardname")
    public String cardname;

    @SerializedName("first6cardno")
    public String first6cardno;

    @SerializedName("last4cardno")
    public String last4cardno;

    @SerializedName("cardhash")
    public String cardhash;

    @SerializedName("expiretime")
    public long expiretime;

    public MapCard() {
        bankcode = "";
    }

    /***
     * convert PaymentCard to MapCard
     */
    public MapCard(PaymentCard pCard) {
        this.expiretime = 0;
        this.bankcode = pCard.getBankcode();
        this.cardname = pCard.getCardholdername();
        if (!TextUtils.isEmpty(pCard.getCardnumber())) {
            this.first6cardno = pCard.getCardnumber().substring(0, 6);
            this.last4cardno = pCard.getCardnumber().substring(pCard.getCardnumber().length() - 4);
        }
    }

    public MapCard clone() {
        MapCard mapCard = new MapCard();
        mapCard.cardname = this.cardname;
        mapCard.first6cardno = this.first6cardno;
        mapCard.last4cardno = this.last4cardno;
        mapCard.bankcode = this.bankcode;
        mapCard.expiretime = this.expiretime;
        mapCard.cardhash = this.cardhash;
        return mapCard;
    }

    @Override
    public String getKey() {
        return first6cardno + last4cardno;
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
    public boolean equals(Object obj) {
        if (!(obj instanceof MapCard)) {
            return false;
        }
        MapCard other = (MapCard) obj;
        return !TextUtils.isEmpty(this.getFirstNumber()) && !TextUtils.isEmpty(this.getLastNumber()) &&
                this.getFirstNumber().equals(other.first6cardno) && this.getLastNumber().equals(other.last4cardno);
    }
}
