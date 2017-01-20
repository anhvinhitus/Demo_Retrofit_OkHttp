package vn.com.vng.zalopay.bank.models;

/**
 * Created by longlv on 1/17/17.
 * *
 */

public class BankAccount {

    public String mFirstAccountNo;
    public String mLastAccountNo;
    public String mFirst6CardNo;
    public String mLast4CardNo;
    public String mBankCode;

    public BankAccount(String firstaccountno,
                       String lastaccountno,
                       String first6cardno,
                       String last4cardno,
                       String bankcode) {
        this.mFirstAccountNo = firstaccountno;
        this.mLastAccountNo = lastaccountno;
        this.mFirst6CardNo = first6cardno;
        this.mLast4CardNo = last4cardno;
        this.mBankCode = bankcode;
    }

    public String getAccountNo() {
        return mLastAccountNo + " " + mFirstAccountNo;
    }
}
