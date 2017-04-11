package vn.com.vng.zalopay.bank.models;

import android.text.TextUtils;

import timber.log.Timber;

/**
 * Created by longlv on 1/17/17.
 * Wrapper of DBankAccount (in PaymentSDK).
 */

public class BankAccount {
    private final int FIRST_NUMBER_SHOW = 3;
    private final int LAST_NUMBER_SHOW = 3;

    public String mFirstAccountNo;
    public String mLastAccountNo;
    public String mFirst6PhoneNumber;
    public String mLast4PhoneNumber;
    public String mBankCode;

    public BankAccount(String firstAccountNo,
                       String lastAccountNo,
                       String first6CardNo,
                       String last4CardNo,
                       String bankCode) {
        this.mFirstAccountNo = firstAccountNo;
        this.mLastAccountNo = lastAccountNo;
        this.mFirst6PhoneNumber = first6CardNo;
        this.mLast4PhoneNumber = last4CardNo;
        this.mBankCode = bankCode;
    }

    public String getPhoneNumber() {
        String phoneNumber = mFirst6PhoneNumber + mLast4PhoneNumber;
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
}
