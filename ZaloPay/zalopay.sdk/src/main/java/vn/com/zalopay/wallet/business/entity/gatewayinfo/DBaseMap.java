package vn.com.zalopay.wallet.business.entity.gatewayinfo;


import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;

public abstract class DBaseMap {
    public String bankcode;

    public abstract String getCardKey();

    public abstract String getFirstNumber();

    public abstract void setFirstNumber(String pFirstNumber);

    public abstract String getLastNumber();

    public abstract void setLastNumber(String pLastNumber);

    public abstract boolean isValid();

    public ECardType getCardType() {
        ECardType eCardType = ECardType.UNDEFINE;

        if (BankLoader.mapBank != null && BankLoader.mapBank.size() > 0) {
            if (BankLoader.mapBank.containsValue(bankcode)) {
                eCardType = ECardType.fromString(bankcode);
            }
        }

        return eCardType;
    }
}
