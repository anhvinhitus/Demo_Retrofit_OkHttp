package vn.com.vng.zalopay.bank;

import android.text.TextUtils;

import java.util.HashMap;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.bank.models.BankCardStyle;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;

/**
 * Created by longlv on 1/17/17.
 * *
 */

public class BankUtils {

    private final static HashMap<String, BankCardStyle> mBankSettings = new HashMap<>();
    private final static BankCardStyle BANK_DEFAULT = new BankCardStyle(0, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end);

    static {
        mBankSettings.put(ECardType.JCB.toString(), new BankCardStyle(R.drawable.ic_jcb, R.color.bg_jcb_start, R.color.bg_jcb_end));
        mBankSettings.put(ECardType.VISA.toString(), new BankCardStyle(R.drawable.ic_visa, R.color.bg_visa_start, R.color.bg_visa_end));
        mBankSettings.put(ECardType.MASTER.toString(), new BankCardStyle(R.drawable.ic_mastercard, R.color.bg_master_card_start, R.color.bg_master_card_end));
        mBankSettings.put(ECardType.PVTB.toString(), new BankCardStyle(R.drawable.ic_vietinbank, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end));
        mBankSettings.put(ECardType.PBIDV.toString(), new BankCardStyle(R.drawable.ic_bidv, R.color.bg_bidv_start, R.color.bg_bidv_end));
        mBankSettings.put(ECardType.PVCB.toString(), new BankCardStyle(R.drawable.ic_vietcombank, R.color.bg_vietcombank_start, R.color.bg_vietcombank_end));
        mBankSettings.put(ECardType.PSCB.toString(), new BankCardStyle(R.drawable.ic_sacombank, R.color.bg_sacombank_start, R.color.bg_sacombank_end));
        /*mBankSettings.put(ECardType.PEIB.toString(), new BankCardStyle(R.drawable.ic_eximbank, R.color.bg_eximbank_start, R.color.bg_eximbank_end));
        mBankSettings.put(ECardType.PAGB.toString(), new BankCardStyle(R.drawable.ic_agribank, R.color.bg_agribank_start, R.color.bg_agribank_end));
        mBankSettings.put(ECardType.PTPB.toString(), new BankCardStyle(R.drawable.ic_tpbank, R.color.bg_tpbank_start, R.color.bg_tpbank_end));*/
        mBankSettings.put(ECardType.UNDEFINE.toString(), BANK_DEFAULT);
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
}
