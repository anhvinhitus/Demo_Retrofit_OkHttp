package vn.com.zalopay.wallet.business.entity.base;

import android.text.TextUtils;

import vn.com.zalopay.wallet.utils.Log;

public class DPaymentCard {
    protected String cardnumber;
    protected String cardholdername;
    protected String cardvalidfrom;
    protected String cardvalidto;
    protected String cvv;

    protected String bankcode;

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getCardholdername() {
        return cardholdername;
    }

    public void setCardholdername(String cardholdername) {
        this.cardholdername = cardholdername;
    }

    public String getCardvalidfrom() {
        return cardvalidfrom;
    }

    public void setCardvalidfrom(String cardvalidfrom) {
        this.cardvalidfrom = cardvalidfrom;
    }

    public String getCardvalidto() {
        return cardvalidto;
    }

    public void setCardvalidto(String cardvalidto) {
        this.cardvalidto = cardvalidto;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getBankcode() {
        return bankcode;
    }

    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
    }

    public String getCardKey() {
        if (TextUtils.isEmpty(cardnumber) || cardnumber.length() < 6) {
            Log.d(this, "===getCardKey()=NULL");
            return null;
        }

        return cardnumber.substring(0, 6) + cardnumber.substring(cardnumber.length() - 4);
    }
}
