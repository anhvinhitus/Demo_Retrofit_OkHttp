package vn.com.zalopay.wallet.business.entity.atm;

public class BankFunction {
    public String bankcode;
    public String bankfunction;
    public int status;
    public Long maintenancefrom;
    public Long maintenanceto;

    public boolean isFunctionMaintenance() {
        return status == 2;
    }
}
