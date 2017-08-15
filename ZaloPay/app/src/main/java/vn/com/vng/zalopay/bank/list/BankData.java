package vn.com.vng.zalopay.bank.list;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.bank.models.BankCardStyle;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.zalopay.wallet.entity.bank.BankAccount;
import vn.com.zalopay.wallet.entity.bank.BaseMap;
import vn.com.zalopay.wallet.entity.bank.MapCard;
import vn.com.zalopay.wallet.constants.CardType;

/**
 * Created by hieuvm on 7/10/17.
 * *
 */

public class BankData {

    @NonNull
    final BaseMap mBaseMap;

    @NonNull
    final String mBankInfo;

    final BankCardStyle mBankCardStyle;

    public BankData(@NonNull BankAccount baseMap, String phonenumber) {
        mBaseMap = baseMap;
        mBankInfo = getBankInfo(baseMap, phonenumber);
        mBankCardStyle = BankUtils.getBankCardStyle(baseMap);
    }

    public BankData(@NonNull MapCard mapCard) {
        mBaseMap = mapCard;
        mBankInfo = getCardNumber(mapCard);
        mBankCardStyle = BankUtils.getBankCardStyle(mapCard);
    }

    private String getBankInfo(BankAccount bankAccount, String phonenumber) {

        if (CardType.PVCB.equalsIgnoreCase(bankAccount.bankcode)) {
            return PhoneUtil.getPhoneNumberScreened(phonenumber);
        } else {
            return bankAccount.firstaccountno + bankAccount.lastaccountno;
        }
    }

    private String getCardNumber(MapCard baseMap) {
        return BankUtils.formatBankCardNumber(baseMap.getFirstNumber(), baseMap.getLastNumber());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BankData data = (BankData) o;

        return mBaseMap.equals(data.mBaseMap);

    }
}
