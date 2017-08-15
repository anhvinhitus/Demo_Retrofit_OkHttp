package vn.com.zalopay.wallet.entity.bank;


import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class BankAccount extends BaseMap {

    @SerializedName("firstaccountno")
    public String firstaccountno;

    @SerializedName("lastaccountno")
    public String lastaccountno;

    @Override
    public String getFirstNumber() {
        return firstaccountno;
    }

    @Override
    public void setFirstNumber(String pFirstNumber) {
        firstaccountno = pFirstNumber;
    }

    @Override
    public String getLastNumber() {
        return lastaccountno;
    }

    @Override
    public void setLastNumber(String pLastNumber) {
        lastaccountno = pLastNumber;
    }

    @Override
    public boolean isValid() {
        return (!TextUtils.isEmpty(getFirstNumber()) && !TextUtils.isEmpty(getLastNumber()));
    }

    @Override
    public String getKey() {
        return firstaccountno + lastaccountno;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof BankAccount) {
            BankAccount other = (BankAccount) obj;
            return this.bankcode.equals(other.bankcode);
        }
        return false;
    }
}
