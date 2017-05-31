package vn.com.vng.zalopay.bank;

import android.text.TextUtils;

import java.util.HashMap;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.bank.models.BankAccountStyle;
import vn.com.vng.zalopay.bank.models.BankCardStyle;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.utils.GsonUtils;

/**
 * Created by longlv on 1/17/17.
 * *
 */

public class BankUtils {

    private final static HashMap<String, BankCardStyle> mBankSettings = new HashMap<>();
    private final static BankAccountStyle BANK_ACCOUNT_DEFAULT;
    private final static HashMap<String, BankAccountStyle> mBankAccountStyles = new HashMap<>();
    private final static BankCardStyle BANK_DEFAULT;

    static {
        BANK_DEFAULT = new BankCardStyle(0, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end);
        mBankSettings.put(CardType.JCB, new BankCardStyle(R.string.ic_jcb, R.color.bg_jcb_start, R.color.bg_jcb_end));
        mBankSettings.put(CardType.VISA, new BankCardStyle(R.string.ic_visa, R.color.bg_visa_start, R.color.bg_visa_end));
        mBankSettings.put(CardType.MASTER, new BankCardStyle(R.string.ic_mastercard, R.color.bg_master_card_start, R.color.bg_master_card_end));
        mBankSettings.put(CardType.PVTB, new BankCardStyle(R.string.ic_vietinbank, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end));
        mBankSettings.put(CardType.PBIDV, new BankCardStyle(R.string.ic_bidv, R.color.bg_bidv_start, R.color.bg_bidv_end));
        mBankSettings.put(CardType.PVCB, new BankCardStyle(R.string.ic_vietcombank, R.color.bg_vietcombank_start, R.color.bg_vietcombank_end));
        mBankSettings.put(CardType.PSCB, new BankCardStyle(R.string.ic_sacombank, R.color.bg_sacombank_start, R.color.bg_sacombank_end));
        mBankSettings.put(CardType.PSGCB, new BankCardStyle(R.string.ic_sgcb, R.color.bg_commercialbank_start, R.color.bg_commercialbank_end));
        /*mBankSettings.put(CardType.PEIB.toString(), new BankCardStyle(R.string.ic_eximbank, R.color.bg_eximbank_start, R.color.bg_eximbank_end));
        mBankSettings.put(CardType.PAGB.toString(), new BankCardStyle(R.string.ic_agribank, R.color.bg_agribank_start, R.color.bg_agribank_end));
        mBankSettings.put(CardType.PTPB.toString(), new BankCardStyle(R.string.ic_tpbank, R.color.bg_tpbank_start, R.color.bg_tpbank_end));*/
        mBankSettings.put(CardType.UNDEFINE, BANK_DEFAULT);

        BANK_ACCOUNT_DEFAULT = new BankAccountStyle(0, R.color.bg_line_sacombank);
        mBankAccountStyles.put(CardType.PSCB, new BankAccountStyle(R.string.ic_sacombank_large, R.color.bg_line_sacombank));
        mBankAccountStyles.put(CardType.PVCB, new BankAccountStyle(R.string.ic_vietcombank_large, R.color.bg_line_vietcombank));
        mBankAccountStyles.put(CardType.PVTB, new BankAccountStyle(R.string.ic_vietinbank_large, R.color.bg_line_vietinbank));
    }

    public static String formatBankCardNumber(String first6cardno, String last4cardno) {
        if (TextUtils.isEmpty(first6cardno) || TextUtils.isEmpty(last4cardno)) {
            return "";
        }
        String bankCardNumber = String.format("%s••••••%s", first6cardno, last4cardno);
        bankCardNumber = bankCardNumber.replaceAll("(.{4})(?=.)", "$1 ");
        return bankCardNumber;
    }

    public static String formatBankCardNumber(String bankCardNumber) {
        if (TextUtils.isEmpty(bankCardNumber)) {
            return "";
        }
        return bankCardNumber.replaceAll("(.{4})(?=.)", "$1 ");
    }

    public static BankCardStyle getBankCardStyle(BankCard bankCard) {
        if (bankCard == null || TextUtils.isEmpty(bankCard.type)) {
            return BANK_DEFAULT;
        }

        if (mBankSettings.containsKey(bankCard.type)) {
            return mBankSettings.get(bankCard.type);
        } else {
            return BANK_DEFAULT;
        }
    }

    public static BankAccountStyle getBankAccountStyle(BankAccount bankAccount) {
        if (bankAccount == null || TextUtils.isEmpty(bankAccount.mBankCode)) {
            return BANK_ACCOUNT_DEFAULT;
        }

        if (mBankAccountStyles.containsKey(bankAccount.mBankCode)) {
            return mBankAccountStyles.get(bankAccount.mBankCode);
        } else {
            return BANK_ACCOUNT_DEFAULT;
        }
    }

    public static BankCardStyle getBankCardStyle(DBaseMap bankCard) {
        if (bankCard == null || TextUtils.isEmpty(bankCard.bankcode)) {
            return BANK_DEFAULT;
        }

        if (mBankSettings.containsKey(bankCard.bankcode)) {
            return mBankSettings.get(bankCard.bankcode);
        } else {
            return BANK_DEFAULT;
        }
    }

    public static BankAccountStyle getBankAccountStyle(DBankAccount bankAccount) {
        if (bankAccount == null || TextUtils.isEmpty(bankAccount.bankcode)) {
            return BANK_ACCOUNT_DEFAULT;
        }

        if (mBankAccountStyles.containsKey(bankAccount.bankcode)) {
            return mBankAccountStyles.get(bankAccount.bankcode);
        } else {
            return BANK_ACCOUNT_DEFAULT;
        }
    }

    public static String getBankName(String bankCode) {
        if (TextUtils.isEmpty(bankCode)) {
            return "";
        }
        String strBankConfig = "";
        try {
            strBankConfig = SharedPreferencesManager.getInstance().getBankConfig(bankCode);
        } catch (Exception e) {
            Timber.w(e, "Function getBankName throw exception [%s]", e.getMessage());
        }
        if (TextUtils.isEmpty(strBankConfig)) {
            return "";
        }
        BankConfig bankConfig = GsonUtils.fromJsonString(strBankConfig, BankConfig.class);
        if (bankConfig == null || TextUtils.isEmpty(bankConfig.name)) {
            return "";
        }
        if (bankConfig.name.startsWith("NH")) {
            return bankConfig.name.substring(2);
        } else {
            return bankConfig.name;
        }
    }
}