package vn.com.zalopay.wallet.business.entity.gatewayinfo;


import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.utils.Log;

public class DBankAccount extends DBaseMap {
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
        return true;
    }

    @Override
    public String getCardKey() {
        String userId = null;
        try {
            userId = GlobalData.getPaymentInfo().userInfo.zaloPayUserId;
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return userId + bankcode;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof DBankAccount) {
            DBankAccount other = (DBankAccount) obj;
            return this.bankcode.equals(other.bankcode);
        }
        return false;
    }
}
