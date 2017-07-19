package vn.com.vng.zalopay.bank;

import android.text.TextUtils;

import java.util.HashMap;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankCardStyle;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.controller.SDKApplication;

/**
 * Created by longlv on 1/17/17.
 * *
 */

public class BankUtils {

    private final static HashMap<String, BankCardStyle> mBankSettings = new HashMap<>();
    private final static BankCardStyle BANK_DEFAULT;

    static {
        BANK_DEFAULT = new BankCardStyle(0, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end);

        // Old version code
//        mBankSettings.put(CardType.JCB, new BankCardStyle(R.string.ic_jcb, R.color.bg_jcb_start, R.color.bg_jcb_end));
//        mBankSettings.put(CardType.VISA, new BankCardStyle(R.string.ic_visa, R.color.bg_visa_start, R.color.bg_visa_end));
//        mBankSettings.put(CardType.MASTER, new BankCardStyle(R.string.ic_mastercard, R.color.bg_master_card_start, R.color.bg_master_card_end));
//        mBankSettings.put(CardType.PVTB, new BankCardStyle(R.string.ic_vietinbank, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end));
//        mBankSettings.put(CardType.PBIDV, new BankCardStyle(R.string.ic_bidv, R.color.bg_bidv_start, R.color.bg_bidv_end));
//        mBankSettings.put(CardType.PVCB, new BankCardStyle(R.string.ic_vietcombank, R.color.bg_vietcombank_start, R.color.bg_vietcombank_end));
//        mBankSettings.put(CardType.PSCB, new BankCardStyle(R.string.ic_sacombank, R.color.bg_sacombank_start, R.color.bg_sacombank_end));
//        mBankSettings.put(CardType.PSGCB, new BankCardStyle(R.string.ic_sgcb, R.color.bg_commercialbank_start, R.color.bg_commercialbank_end));
//        /*mBankSettings.put(CardType.PEIB.toString(), new BankCardStyle(R.string.ic_eximbank, R.color.bg_eximbank_start, R.color.bg_eximbank_end));
//        mBankSettings.put(CardType.PAGB.toString(), new BankCardStyle(R.string.ic_agribank, R.color.bg_agribank_start, R.color.bg_agribank_end));
//        mBankSettings.put(CardType.PTPB.toString(), new BankCardStyle(R.string.ic_tpbank, R.color.bg_tpbank_start, R.color.bg_tpbank_end));*/
//        mBankSettings.put(CardType.UNDEFINE, BANK_DEFAULT);
//
//        BANK_ACCOUNT_DEFAULT = new BankAccountStyle(0, R.color.bg_line_sacombank);
//        mBankAccountStyles.put(CardType.PSCB, new BankAccountStyle(R.string.ic_sacombank_large, R.color.bg_line_sacombank));
//        mBankAccountStyles.put(CardType.PVCB, new BankAccountStyle(R.string.ic_vietcombank_large, R.color.bg_line_vietcombank));
//        mBankAccountStyles.put(CardType.PVTB, new BankAccountStyle(R.string.ic_vietinbank_large, R.color.bg_line_vietinbank));

        mBankSettings.put(CardType.JCB.toString(), new BankCardStyle(R.string.ic_jcb, R.color.bg_jcb_start, R.color.bg_jcb_end));
        mBankSettings.put(CardType.VISA.toString(), new BankCardStyle(R.string.ic_visa, R.color.bg_visa_start, R.color.bg_visa_end));
        mBankSettings.put(CardType.MASTER.toString(), new BankCardStyle(R.string.ic_mastercard, R.color.bg_master_card_start, R.color.bg_master_card_end));
        mBankSettings.put(CardType.PVTB.toString(), new BankCardStyle(R.string.ic_vietinbank, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end));
        mBankSettings.put(CardType.PBIDV.toString(), new BankCardStyle(R.string.ic_bidv, R.color.bg_bidv_start, R.color.bg_bidv_end));
        mBankSettings.put(CardType.PVCB.toString(), new BankCardStyle(R.string.ic_vietcombank, R.color.bg_vietcombank_start, R.color.bg_vietcombank_end));
        mBankSettings.put(CardType.PSCB.toString(), new BankCardStyle(R.string.ic_sacombank, R.color.bg_sacombank_start, R.color.bg_sacombank_end));
        mBankSettings.put(CardType.PSGCB.toString(), new BankCardStyle(R.string.ic_sgcb, R.color.bg_commercialbank_start, R.color.bg_commercialbank_end));
        mBankSettings.put(CardType.PEIB.toString(), new BankCardStyle(R.string.ic_eximbank, R.color.bg_eximbank_start, R.color.bg_eximbank_end));
        /*mBankSettings.put(ECardType.PAGB.toString(), new BankCardStyle(R.string.ic_agribank, R.color.bg_agribank_start, R.color.bg_agribank_end));
        mBankSettings.put(ECardType.PTPB.toString(), new BankCardStyle(R.string.ic_tpbank, R.color.bg_tpbank_start, R.color.bg_tpbank_end));*/
        mBankSettings.put(CardType.PSCB.toString(), new BankCardStyle(R.string.ic_sacombank, R.color.bg_sacombank_start, R.color.bg_sacombank_end));
        mBankSettings.put(CardType.PVCB.toString(), new BankCardStyle(R.string.ic_vietcombank, R.color.bg_vietcombank_start, R.color.bg_vietcombank_end));
        mBankSettings.put(CardType.UNDEFINE.toString(), BANK_DEFAULT);

    }

    public static String formatBankCardNumber(String first6CardNo, String last4CardNo) {
        if (TextUtils.isEmpty(first6CardNo) || TextUtils.isEmpty(last4CardNo)) {
            return "";
        }
        String bankCardNumber = String.format("%s••••••%s", first6CardNo, last4CardNo);
        bankCardNumber = bankCardNumber.replaceAll("(.{4})(?=.)", "$1 ");
        return bankCardNumber;
    }

    public static String detectCCCard(String first6CardNo, User user) {
        if (user == null) {
            return CardType.UNDEFINE.toString();
        }
        UserInfo userInfo = new UserInfo();
        userInfo.zalopay_userid = user.zaloPayId;
        userInfo.accesstoken = user.accesstoken;

        try {
            return CShareDataWrapper.detectCardType(userInfo, first6CardNo);
        } catch (Exception e) {
            Timber.w(e, "detectCardType exception [%s]", e.getMessage());
        }
        return CardType.UNDEFINE.toString();
    }

    public static BankCardStyle getBankCardStyle(BaseMap bankCard, User user) {
        if (bankCard == null || TextUtils.isEmpty(bankCard.bankcode)) {
            return BANK_DEFAULT;
        }

        Timber.d("getBankCardStyle bankCode [%s]", bankCard.bankcode);
        if (BuildConfig.CC_CODE.equals(bankCard.bankcode)) {
            return mBankSettings.get(detectCCCard(bankCard.getFirstNumber(), user));
        } else if (mBankSettings.containsKey(bankCard.bankcode)) {
            return mBankSettings.get(bankCard.bankcode);
        } else {
            return BANK_DEFAULT;
        }
    }

    public static String getBankName(String bankCode) {
        if (TextUtils.isEmpty(bankCode)) {
            return "";
        }
        try {
            BankConfig bankConfig = SDKApplication.getApplicationComponent()
                    .bankListInteractor()
                    .getBankConfig(bankCode);
            if (bankConfig == null || TextUtils.isEmpty(bankConfig.name)) {
                return "";
            }
            if (bankConfig.name.startsWith("NH")) {
                return bankConfig.name.substring(2);
            } else {
                return bankConfig.name;
            }

        } catch (Exception e) {
            Timber.w(e, "Function getBankName throw exception [%s]", e.getMessage());
        }
        return "";
    }
}