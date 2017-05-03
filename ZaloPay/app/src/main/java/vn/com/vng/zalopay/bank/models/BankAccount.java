package vn.com.vng.zalopay.bank.models;

import android.text.TextUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;

/**
 * Created by longlv on 1/17/17.
 * Wrapper of DBankAccount (in PaymentSDK).
 */

public class BankAccount {

    private String mFirstAccountNo;
    private String mLastAccountNo;
    public String mBankCode;

    public BankAccount(String firstAccountNo,
                       String lastAccountNo,
                       String bankCode) {
        this.mFirstAccountNo = firstAccountNo;
        this.mLastAccountNo = lastAccountNo;
        this.mBankCode = bankCode;
    }

    public String getAccountInfo() {
        if (ECardType.PVCB.toString().equalsIgnoreCase(mBankCode)) {
            return getPhoneNumberScreened(getCurrentUserPhone());
        } else {
            return mFirstAccountNo + mLastAccountNo;
        }
    }

    private String getPhoneNumberScreened(String phoneNumber) {
        final int FIRST_NUMBER_SHOW = 3;
        final int LAST_NUMBER_SHOW = 3;
        try {
            if (TextUtils.isEmpty(phoneNumber)) {
                return "";
            } else if (phoneNumber.length() <= (FIRST_NUMBER_SHOW + LAST_NUMBER_SHOW)) {
                return phoneNumber;
            } else {
                String first3Number = phoneNumber.substring(0, FIRST_NUMBER_SHOW);
                String last3Number = phoneNumber.substring(phoneNumber.length() - LAST_NUMBER_SHOW,
                        phoneNumber.length());
                String betweenNumber = phoneNumber.substring(FIRST_NUMBER_SHOW,
                        phoneNumber.length() - LAST_NUMBER_SHOW);
                return String.format("%s %s %s", first3Number, betweenNumber.replaceAll("\\d", "*"), last3Number);
            }
        } catch (Exception e) {
            Timber.e(e, "Function getPhoneNumber throw exception [%s]", e.getMessage());
        }
        return phoneNumber;
    }

    private String getCurrentUserPhone() {
        String phoneNumber = "";
        UserComponent userComponent = AndroidApplication.instance().getUserComponent();
        if (userComponent == null) {
            return "";
        }
        User user = userComponent.currentUser();
        if (user != null) {
            phoneNumber = PhoneUtil.formatPhoneNumber(user.phonenumber);
        }
        return phoneNumber;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof BankAccount)) {
            return false;
        }
        BankAccount bankAccount = (BankAccount) object;
        return !(TextUtils.isEmpty(bankAccount.mFirstAccountNo)
                || TextUtils.isEmpty(bankAccount.mLastAccountNo)
                || TextUtils.isEmpty(bankAccount.mBankCode))
                && bankAccount.mFirstAccountNo.equals(this.mFirstAccountNo)
                && bankAccount.mLastAccountNo.equals(this.mLastAccountNo)
                && bankAccount.mBankCode.equals(this.mBankCode);
    }
}
