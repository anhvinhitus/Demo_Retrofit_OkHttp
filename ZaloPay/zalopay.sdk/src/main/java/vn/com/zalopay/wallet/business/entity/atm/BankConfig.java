package vn.com.zalopay.wallet.business.entity.atm;

import java.util.List;

import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.behavior.view.paymentfee.CBaseCalculateFee;
import vn.com.zalopay.wallet.business.behavior.view.paymentfee.CWithDrawCalculateFee;
<<<<<<< HEAD
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.BankStatus;
import vn.com.zalopay.wallet.constants.FeeType;
=======
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankStatus;
import vn.com.zalopay.wallet.business.entity.enumeration.EFeeCalType;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ZPWUtils;
>>>>>>> e749e00... [SDK] Message bảo trì theo bank function

public class BankConfig {
    public String code;
    public String name;
    public int banktype;
    public String otptype;
    public String type;
    public int interfacetype;
    public int requireotp;
    public int allowwithdraw = 0;
    public double feerate = -1;
    public double minfee = -1;
    public long maintenancefrom = 0;
    public long maintenanceto = 0;
    public String maintenancemsg = null;
    public int supporttype = 1;
    public double totalfee = 0;
    public String loginbankurl;
    public List<BankFunction> functions = null;

<<<<<<< HEAD
    @FeeType
    public String feecaltype = null;
=======
    /***
     * get detail maintenance message from bankconfig
     * @return
     */
    public static String getFormattedBankMaintenaceMessage() {
        String message = "Ngân hàng đang bảo trì.Vui lòng quay lại sau ít phút.";
        try {
            String maintenanceTo = null;
            BankConfig bankConfig = BankLoader.getInstance().maintenanceBank;
            BankFunction bankFunction = BankLoader.getInstance().maintenanceBankFunction;
            //maintenance all function in bank
            if (bankConfig != null && bankConfig.isBankFunctionAllMaintenance()) {
                message = bankConfig.maintenancemsg;
                if (bankConfig.maintenanceto > 0) {
                    maintenanceTo = ZPWUtils.convertDateTime(bankConfig.maintenanceto);
                }
                if (!TextUtils.isEmpty(message) && message.contains("%s")) {
                    message = String.format(message, maintenanceTo);
                } else if (TextUtils.isEmpty(message)) {
                    message = GlobalData.getStringResource(RS.string.zpw_string_bank_maitenance);
                    message = String.format(message, bankConfig.name, maintenanceTo);
                }
            } else if (bankFunction != null && bankFunction.isFunctionMaintenance()) {
                //maintenance some function of bank
                message = bankFunction.maintenancemsg;
                if (bankFunction.maintenanceto > 0) {
                    maintenanceTo = ZPWUtils.convertDateTime(bankFunction.maintenanceto);
                }
                if (!TextUtils.isEmpty(message) && message.contains("%s")) {
                    message = String.format(message, maintenanceTo);
                } else if (TextUtils.isEmpty(message)) {
                    message = GlobalData.getStringResource(RS.string.zpw_string_bank_maitenance);
                    message = String.format(message, bankConfig.name, maintenanceTo);
                }
            }
        } catch (Exception ex) {
            Log.e("getFormattedBankMaintenaceMessage", ex);
        }
        return message;
    }

    public String getShortBankName() {
        if (!TextUtils.isEmpty(name) && name.startsWith("NH")) {
            return name.substring(2);
        }
        return name;
    }
>>>>>>> e749e00... [SDK] Message bảo trì theo bank function

<<<<<<< HEAD
    @BankStatus
    public int status;
=======
    public boolean isVersionSupport(String pAppVersion) {
        Log.d(this, "start check support bank version in bank config");
        if (TextUtils.isEmpty(pAppVersion)) {
            return true;
        }
        int minAppVersionSupport = getMinAppVersionSupport();
        if (minAppVersionSupport == 0) {
            return true;
        }
        pAppVersion = pAppVersion.replace(".", "");
        return Integer.parseInt(pAppVersion) >= minAppVersionSupport;
    }
>>>>>>> 9fd9a35... [SDK] Apply app info v1

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

<<<<<<< HEAD
    public boolean isBankMaintenence(@BankFunctionCode int pBankFunction) {
=======
    public boolean isBankMaintenence(EBankFunction pBankFunction) {
        Log.d(this, "isBankMaintenence " + ((pBankFunction != null) ? pBankFunction.toString() : "NULL"));
>>>>>>> e749e00... [SDK] Message bảo trì theo bank function
        return isBankFunctionAllMaintenance() || isBankFunctionMaintenance(pBankFunction);
    }

    /***
     * bank maintenance all functions
     * @return
     */
    public boolean isBankFunctionAllMaintenance() {
        return status == BankStatus.MAINTENANCE;
    }

    /***
     * check this bank is active for payment
     * @return
     */
    public boolean isBankActive() {
        return status == BankStatus.ACTIVE;
    }

    /***
     * bank maintenance by function: withdraw, link card...
     * @param pBankFunction
     * @return
     */
    public boolean isBankFunctionMaintenance(@BankFunctionCode int pBankFunction) {
        if (functions == null) {
            return false;
        }
        BankFunction bankFunction = null;
        for (int i = 0; i < functions.size(); i++) {
            if (functions.get(i).bankfunction == pBankFunction) {
                bankFunction = functions.get(i);
                break;
            }
        }
        return bankFunction != null && bankFunction.isFunctionMaintenance();
    }

    public BankFunction getBankFunction(@BankFunctionCode int pBankFunction) {
        if (functions == null) {
            return null;
        }

        for (int i = 0; i < functions.size(); i++) {
            if (functions.get(i).bankfunction == pBankFunction) {
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
