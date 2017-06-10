package vn.com.zalopay.wallet.business.entity.atm;

import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.BankStatus;

public class BankFunction {
    public String bankcode;
    @BankStatus
    public int status;
    public String maintenancemsg;
    public Long maintenancefrom;
    public Long maintenanceto;
    @BankFunctionCode
    public int bankfunction;

    public boolean isFunctionMaintenance() {
        return status == BankStatus.MAINTENANCE;
    }
}
