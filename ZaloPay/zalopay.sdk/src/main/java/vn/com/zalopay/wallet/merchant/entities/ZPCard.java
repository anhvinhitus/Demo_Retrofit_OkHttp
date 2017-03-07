package vn.com.zalopay.wallet.merchant.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * used by app
 */

public class ZPCard implements Parcelable {
    public static final Creator<ZPCard> CREATOR = new Creator<ZPCard>() {
        @Override
        public ZPCard createFromParcel(Parcel source) {
            return new ZPCard(source);
        }

        @Override
        public ZPCard[] newArray(int size) {
            return new ZPCard[size];
        }
    };
    protected String cardCode;
    protected String cardLogoName;
    protected Boolean isBankAccount;

    public ZPCard(String pCardCode, String pCardLogoName) {
        this.cardCode = pCardCode;
        this.cardLogoName = pCardLogoName;
        this.isBankAccount = false;
    }

    public ZPCard(String pCardCode, String pCardLogoName, Boolean pIsBankAccount) {
        this.cardCode = pCardCode;
        this.cardLogoName = pCardLogoName;
        this.isBankAccount = pIsBankAccount;
    }

    public ZPCard(Parcel in) {
        this.cardCode = in.readString();
        this.cardLogoName = in.readString();
        this.isBankAccount = (Boolean) in.readValue(null);
    }

    public Boolean isBankAccount() {
        return isBankAccount;
    }

    public void setIsBankAccount(Boolean pIsBankAccount) {
        isBankAccount = pIsBankAccount;
    }

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;

        if (object != null && object instanceof ZPCard) {
            ZPCard other = (ZPCard) object;

            if (object != null && !TextUtils.isEmpty(((ZPCard) object).cardCode))
                sameSame = this.cardCode.equals(other.cardCode);
        }

        return sameSame;
    }

    public String getCardLogoName() {
        return cardLogoName;
    }

    public void setCardLogoName(String cardLogoName) {
        this.cardLogoName = cardLogoName;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(this.cardCode);
        dest.writeString(this.cardLogoName);
        dest.writeValue(this.isBankAccount);
    }
}
