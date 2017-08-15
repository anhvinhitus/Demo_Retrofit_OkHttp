package vn.com.zalopay.wallet.entity.bank;

import com.google.gson.annotations.SerializedName;

import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.BankStatus;

public class BankFunction {
    @SerializedName("bankcode")
    public String bankcode;

    @SerializedName("status")
    @BankStatus
    public int status;

    @SerializedName("maintenancemsg")
    public String maintenancemsg;

    @SerializedName("maintenanceto")
    public Long maintenanceto;

    @SerializedName("bankfunction")
    @BankFunctionCode
    public int bankfunction;

    public boolean isFunctionMaintenance() {
        return status == BankStatus.MAINTENANCE;
    }
}
