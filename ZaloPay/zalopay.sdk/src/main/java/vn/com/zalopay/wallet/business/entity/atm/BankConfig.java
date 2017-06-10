package vn.com.zalopay.wallet.business.entity.atm;

import android.text.TextUtils;

import java.util.List;

import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.BankStatus;

public class BankConfig {
    public String code;
    public String name;
    public String fullname;
    public int banktype;
    public String otptype;
    public String type;
    public int interfacetype;
    public int allowwithdraw = 0;
    public long maintenancefrom = 0;
    public long maintenanceto = 0;
    public String maintenancemsg = null;
    public int supporttype = 1;
    public String loginbankurl;
    public List<BankFunction> functions = null;
    @BankStatus
    public int status;
    public int displayorder = 0;// order sort in UI

    public String getDisplayName() {
        return !TextUtils.isEmpty(fullname) ? fullname : getShortBankName();
    }

    public String getShortBankName() {
        if (!TextUtils.isEmpty(name) && name.startsWith("NH")) {
            return name.substring(2);
        }
        return name;
    }

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

    public boolean isBankMaintenence(@BankFunctionCode int pBankFunction) {
        return isMaintenanceAllFunctions() || isBankFunctionMaintenance(pBankFunction);
    }

    /***
     * bank maintenance all functions
     * @return
     */
    public boolean isMaintenanceAllFunctions() {
        return status == BankStatus.MAINTENANCE;
    }

    /***
     * check this bank is active for payment
     * @return
     */
    public boolean isActive() {
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
    public boolean isParseWebsite() {
        return interfacetype == 1;
    }

    //can bank allow withdrawing
    public boolean isWithDrawAllow() {
        return allowwithdraw == 1;
    }

    public boolean isBankAccount() {
        return supporttype == 2;
    }

    /***
     * get detail maintenance message from bankconfig
     * @return
     */
    public String getMaintenanceMessage(@BankFunctionCode int bankFunctionCode) {
        String message = "Ngân hàng đang bảo trì.Vui lòng quay lại sau ít phút.";
        try {
            String maintenanceTo = null;
            BankFunction bankFunction = getBankFunction(bankFunctionCode);
            //maintenance all function in bank
            if (isMaintenanceAllFunctions()) {
                message = maintenancemsg;
                if (maintenanceto > 0) {
                    maintenanceTo = SdkUtils.convertDateTime(maintenanceto);
                }
                if (!TextUtils.isEmpty(message) && message.contains("%s")) {
                    message = String.format(message, maintenanceTo);
                } else if (TextUtils.isEmpty(message)) {
                    message = GlobalData.getStringResource(RS.string.zpw_string_bank_maitenance);
                    message = String.format(message, name, maintenanceTo);
                }
            } else if (bankFunction != null && bankFunction.isFunctionMaintenance()) {
                //maintenance some function of bank
                message = bankFunction.maintenancemsg;
                if (bankFunction.maintenanceto > 0) {
                    maintenanceTo = SdkUtils.convertDateTime(bankFunction.maintenanceto);
                }
                if (!TextUtils.isEmpty(message) && message.contains("%s")) {
                    message = String.format(message, maintenanceTo);
                } else if (TextUtils.isEmpty(message)) {
                    message = GlobalData.getStringResource(RS.string.zpw_string_bank_maitenance);
                    message = String.format(message, name, maintenanceTo);
                }
            }
        } catch (Exception ex) {
            Log.e("getMaintenanceMessage", ex);
        }
        return message;
    }

}
