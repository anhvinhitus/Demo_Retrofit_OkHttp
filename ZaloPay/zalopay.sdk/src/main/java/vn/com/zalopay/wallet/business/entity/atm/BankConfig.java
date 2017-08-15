package vn.com.zalopay.wallet.business.entity.atm;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.BankStatus;

public class BankConfig {
    @SerializedName("code")
    public String code;

    @SerializedName("name")
    public String name;

    @SerializedName("fullname")
    public String fullname;

    @SerializedName("banktype")
    public int banktype;

    @SerializedName("otptype")
    public String otptype;

    @SerializedName("type")
    public String type;

    @SerializedName("interfacetype")
    public int interfacetype;

    @SerializedName("allowwithdraw")
    public int allowwithdraw = 0;

    @SerializedName("maintenanceto")
    public long maintenanceto = 0;

    @SerializedName("maintenancemsg")
    public String maintenancemsg;

    @SerializedName("supporttype")
    public int supporttype = 1;

    @SerializedName("loginbankurl")
    public String loginbankurl;

    @SerializedName("functions")
    public List<BankFunction> functions;

    @SerializedName("bankLogo")
    public String bankLogo;

    @SerializedName("status")
    @BankStatus
    public int status;

    @SerializedName("displayorder")
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
    private boolean isMaintenanceAllFunctions() {
        return status == BankStatus.MAINTENANCE;
    }

    /*
     * check this bank is active for payment
     */
    public boolean isActive() {
        return status == BankStatus.ACTIVE;
    }

    /*
     * bank maintenance by function: withdraw, link card
     */
    private boolean isBankFunctionMaintenance(@BankFunctionCode int pBankFunction) {
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

    /*
     * get detail maintenance message from bankconfig
     */
    public String getMaintenanceMessage(@BankFunctionCode int bankFunctionCode) {
        String message = "Ngân hàng đang bảo trì.Vui lòng quay lại sau ít phút.";
        try {
            String maintenanceTo = "";
            //maintenance all function in bank
            if (isMaintenanceAllFunctions()) {
                if (!TextUtils.isEmpty(maintenancemsg)) {
                    message = maintenancemsg;
                }
                if (maintenanceto > 0) {
                    maintenanceTo = SdkUtils.convertDateTime(maintenanceto);
                }
                if (message.contains("%s") && !TextUtils.isEmpty(maintenanceTo)) {
                    message = String.format(message, maintenanceTo);
                    return message;
                }
                return message;
            }
            BankFunction bankFunction = getBankFunction(bankFunctionCode);
            if (bankFunction != null && bankFunction.isFunctionMaintenance()) {
                if (!TextUtils.isEmpty(bankFunction.maintenancemsg)) {
                    message = bankFunction.maintenancemsg;
                }
                if (bankFunction.maintenanceto > 0) {
                    maintenanceTo = SdkUtils.convertDateTime(bankFunction.maintenanceto);
                }
                if (message.contains("%s") && !TextUtils.isEmpty(maintenanceTo)) {
                    message = String.format(message, maintenanceTo);
                    return message;
                }
                return message;
            }
        } catch (Exception ex) {
            Timber.w(ex, "Exception get bank maintenance message");
        }
        return message;
    }

}
