package vn.com.vng.zalopay.bank;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankCardStyle;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.entity.bank.BankConfig;
import vn.com.zalopay.wallet.entity.bank.BankAccount;
import vn.com.zalopay.wallet.entity.bank.BaseMap;
import vn.com.zalopay.wallet.entity.bank.MapCard;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.controller.SDKApplication;

/**
 * Created by longlv on 1/17/17.
 * *
 */

public class BankUtils {

    private final static HashMap<String, BankCardStyle> mBankSettings = new HashMap<>();

    static {
        mBankSettings.put(CardType.JCB, new BankCardStyle(R.string.ic_jcb, R.color.bg_jcb_start, R.color.bg_jcb_end));
        mBankSettings.put(CardType.VISA, new BankCardStyle(R.string.ic_visa, R.color.bg_visa_start, R.color.bg_visa_end));
        mBankSettings.put(CardType.MASTER, new BankCardStyle(R.string.ic_mastercard, R.color.bg_master_card_start, R.color.bg_master_card_end));
        mBankSettings.put(CardType.PVTB, new BankCardStyle(R.string.ic_vietinbank, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end));
        mBankSettings.put(CardType.PBIDV, new BankCardStyle(R.string.ic_bidv, R.color.bg_bidv_start, R.color.bg_bidv_end));
        mBankSettings.put(CardType.PVCB, new BankCardStyle(R.string.ic_vietcombank, R.color.bg_vietcombank_start, R.color.bg_vietcombank_end));
        mBankSettings.put(CardType.PSCB, new BankCardStyle(R.string.ic_sacombank, R.color.bg_sacombank_start, R.color.bg_sacombank_end));
        mBankSettings.put(CardType.PSGCB, new BankCardStyle(R.string.ic_sgcb, R.color.bg_commercialbank_start, R.color.bg_commercialbank_end));
        mBankSettings.put(CardType.PEIB, new BankCardStyle(R.string.ic_eximbank, R.color.bg_eximbank_start, R.color.bg_eximbank_end));
        mBankSettings.put(CardType.PSCB, new BankCardStyle(R.string.ic_sacombank, R.color.bg_sacombank_start, R.color.bg_sacombank_end));
        mBankSettings.put(CardType.PVCB, new BankCardStyle(R.string.ic_vietcombank, R.color.bg_vietcombank_start, R.color.bg_vietcombank_end));
        mBankSettings.put(CardType.UNDEFINE, new BankCardStyle(0, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end));
    }

    public static String formatBankCardNumber(String first6CardNo, String last4CardNo) {
        if (TextUtils.isEmpty(first6CardNo) || TextUtils.isEmpty(last4CardNo)) {
            return "";
        }
        String bankCardNumber = String.format("%s••••••%s", first6CardNo, last4CardNo);
        bankCardNumber = bankCardNumber.replaceAll("(.{4})(?=.)", "$1 ");
        return bankCardNumber;
    }

    public static BankCardStyle getBankCardStyle(BaseMap bankCard) {
        if (bankCard == null || TextUtils.isEmpty(bankCard.bankcode)) {
            return mBankSettings.get(CardType.UNDEFINE);
        }

        Timber.d("getBankCardStyle bankCode [%s]", bankCard.bankcode);
        if (BuildConfig.CC_CODE.equals(bankCard.bankcode)) {
            return mBankSettings.get(CShareDataWrapper.detectCCCard(bankCard.getFirstNumber()));
        } else if (mBankSettings.containsKey(bankCard.bankcode)) {
            return mBankSettings.get(bankCard.bankcode);
        } else {
            return mBankSettings.get(CardType.UNDEFINE);
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

    public static List<BankAccount> getFake() {
        List<BankAccount> linkedBankList = new ArrayList<>();
        BankAccount vcbAccount = new BankAccount();
        vcbAccount.firstaccountno = "098765";
        vcbAccount.lastaccountno = "4321";
        vcbAccount.bankcode = CardType.PVCB;
        linkedBankList.add(vcbAccount);
        return linkedBankList;
    }

    public static List<MapCard> getFakeData() {
        List<MapCard> linkedBankList = new ArrayList<>();

        MapCard visaCard = new MapCard();
        visaCard.first6cardno = "445093";
        visaCard.last4cardno = "0161";
        visaCard.bankcode = CardType.MASTER;
        linkedBankList.add(visaCard);

        MapCard vtbCard = new MapCard();
        vtbCard.bankcode = CardType.PVTB;
        vtbCard.first6cardno = "970415";
        vtbCard.last4cardno = "3538";
        linkedBankList.add(vtbCard);

        MapCard vcbCard = new MapCard();
        vcbCard.bankcode = CardType.PVCB;
        vcbCard.first6cardno = "686868";
        vcbCard.last4cardno = "1231";
        linkedBankList.add(vcbCard);

        MapCard sCard = new MapCard();
        sCard.bankcode = CardType.PSCB;
        sCard.first6cardno = "970403";
        sCard.last4cardno = "1234";
        linkedBankList.add(sCard);

        MapCard sgCard = new MapCard();
        sgCard.bankcode = CardType.PSGCB;
        sgCard.first6cardno = "157979";
        sgCard.last4cardno = "9999";
        linkedBankList.add(sgCard);

        MapCard bivdCard = new MapCard();
        bivdCard.first6cardno = "970418";
        bivdCard.last4cardno = "1231";
        bivdCard.bankcode = CardType.PBIDV;
        linkedBankList.add(bivdCard);

        return linkedBankList;
    }

    public static boolean isLinkedBankAccount(List<BankAccount> banks, String bankCode) {
        if (Lists.isEmptyOrNull(banks)
                || TextUtils.isEmpty(bankCode)) {
            return false;
        }

        for (BankAccount bankAccount : banks) {
            if (bankAccount == null) {
                continue;
            }

            if (bankCode.equalsIgnoreCase(bankAccount.bankcode)) {
                return true;
            }
        }

        return false;
    }

}