package vn.com.zalopay.wallet.business.entity.atm;

import java.util.List;

import vn.com.zalopay.wallet.business.behavior.view.paymentfee.CBaseCalculateFee;
import vn.com.zalopay.wallet.business.behavior.view.paymentfee.CWithDrawCalculateFee;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.entity.enumeration.EFeeCalType;
import vn.com.zalopay.wallet.utils.Log;

public class BankConfig {
    public String code;
    public String name;
    public int banktype;
    public String otptype;
    public String type;
    public int status;
    public int interfacetype;
    public int requireotp;

    public int allowwithdraw = 0;
    public double feerate = -1;
    public double minfee = -1;
    public EFeeCalType feecaltype = EFeeCalType.SUM;

    public long maintenancefrom = 0;
    public long maintenanceto = 0;
    public String maintenancemsg = null;

    public int supporttype = 1;

    public double totalfee = 0;

    public String loginbankurl;

    public List<BankFunction> functions = null;

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;

        if (object != null && object instanceof BankConfig) {
            BankConfig other = (BankConfig) object;

            if (code.equals(other.code)) {
                sameSame = true;
            }
        }

        return sameSame;
    }

    public double calculateFee() {
        totalfee = CBaseCalculateFee.getInstance().setCalculator(new CWithDrawCalculateFee(this)).countFee();

        return totalfee;
    }

    public boolean isBankMaintenence(EBankFunction pBankFunction) {
        Log.d(this, "===isBankMaintenence===pBankFunction=" + ((pBankFunction != null) ? pBankFunction.toString() : "NULL"));
        return isBankFunctionAllMaintenance() || isBankFunctionMaintenance(pBankFunction);
    }

    /***
     * bank maintenance all functions
     *
     * @return
     */
    public boolean isBankFunctionAllMaintenance() {
        return status == 2;
    }

    /***
     * bank maintenance by function: withdraw, link card...
     *
     * @param pBankFunction
     * @return
     */
    public boolean isBankFunctionMaintenance(EBankFunction pBankFunction) {
        if (pBankFunction == null) {
            return false;
        }
        if (functions == null) {
            return false;
        }

        BankFunction bankFunction = null;
        for (int i = 0; i < functions.size(); i++) {
            if (functions.get(i).bankfunction.equalsIgnoreCase(pBankFunction.toString())) {
                bankFunction = functions.get(i);
                break;
            }
        }
        if (bankFunction != null && bankFunction.isFunctionMaintenance()) {
            return true;
        }
        return false;
    }

    public BankFunction getBankFunction(EBankFunction pBankFunction) {
        if (functions == null) {
            return null;
        }

        for (int i = 0; i < functions.size(); i++) {
            if (functions.get(i).bankfunction.equalsIgnoreCase(pBankFunction.toString())) {
                return functions.get(i);
            }
        }
        return null;
    }

    //is bank use webview for hiding bank's website?
    public boolean isCoverBank() {
        return interfacetype == 1;
    }

    //can bank use pin instead of bank's otp?
    public boolean isRequireOtp() {
        return requireotp == 1;
    }

    //can bank allow withdrawing
    public boolean isAllowWithDraw() {
        return allowwithdraw == 1;
    }

    public boolean isBankAccount() {
        return supporttype == 2;
    }
}
