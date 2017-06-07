package vn.com.zalopay.wallet.business.entity.gatewayinfo;


import android.text.TextUtils;

public class BankAccount extends DBaseMap {
    public String firstaccountno = null;
    public String lastaccountno = null;

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
    public String getCardKey(String pUserId) {
        return pUserId + bankcode;
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
