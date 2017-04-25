package vn.com.zalopay.wallet.business.entity.atm;

import vn.com.zalopay.wallet.constants.BankFunctionCode;

public class BankFunction {
    public String bankcode;
    public int status;
    public Long maintenancefrom;
    public Long maintenanceto;
    @BankFunctionCode
    public int bankfunction;

    public boolean isFunctionMaintenance() {
        return status == 2;
    }
}
