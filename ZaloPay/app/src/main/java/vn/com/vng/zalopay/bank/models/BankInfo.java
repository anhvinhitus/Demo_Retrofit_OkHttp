package vn.com.vng.zalopay.bank.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by longlv on 5/29/17.
 * Wrapper BaseMap
 */

public class BankInfo implements Parcelable {

    public BankAction mBankAction;
    public String mBankCode;
    public String mFirstNumber;
    public String mLastNumber;

    public BankInfo(BankAction mBankAction, String mBankCode, String mFirstNumber, String mLastNumber) {
        this.mBankAction = mBankAction;
        this.mBankCode = mBankCode;
        this.mFirstNumber = mFirstNumber;
        this.mLastNumber = mLastNumber;
    }

    protected BankInfo(Parcel in) {
        mBankAction = (BankAction) in.readSerializable();
        mBankCode = in.readString();
        mFirstNumber = in.readString();
        mLastNumber = in.readString();
    }

    public static final Creator<BankInfo> CREATOR = new Creator<BankInfo>() {
        @Override
        public BankInfo createFromParcel(Parcel in) {
            return new BankInfo(in);
        }

        @Override
        public BankInfo[] newArray(int size) {
            return new BankInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mBankAction);
        dest.writeString(mBankCode);
        dest.writeString(mFirstNumber);
        dest.writeString(mLastNumber);
    }
}
