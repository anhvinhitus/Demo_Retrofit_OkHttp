package vn.com.vng.zalopay.transfer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.ZPProfile;

/**
 * Created by hieuvm on 3/31/17.
 * *
 */

public class TransferObject implements Parcelable {

    public long zaloId;
    public String zalopayId;
    public String displayName;
    public String avatar;
    public String zalopayName;
    public String phoneNumber;

    public long amount;
    public String message;

    public Constants.TransferMode transferMode = Constants.TransferMode.TransferToZaloPayContact;
    public Constants.ActivateSource activateSource = Constants.ActivateSource.FromTransferActivity;

    public boolean isEnoughZalopayInfo() {
        return !TextUtils.isEmpty(zalopayId) && !TextUtils.isEmpty(zalopayName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.zaloId);
        dest.writeString(this.zalopayId);
        dest.writeString(this.displayName);
        dest.writeString(this.avatar);
        dest.writeString(this.zalopayName);
        dest.writeString(this.phoneNumber);
        dest.writeLong(this.amount);
        dest.writeString(this.message);
        dest.writeInt(this.transferMode == null ? -1 : this.transferMode.ordinal());
        dest.writeInt(this.activateSource == null ? -1 : this.activateSource.ordinal());
    }

    public TransferObject() {
    }


    public TransferObject(ZPProfile zalo) {
        this.zaloId = zalo.userId;
        this.zalopayId = zalo.zaloPayId;
        this.zalopayName = zalo.zalopayname;
        this.avatar = zalo.avatar;
        this.displayName = zalo.displayName;
        this.phoneNumber = PhoneUtil.formatPhoneNumber(zalo.phonenumber);
    }

    public TransferObject(RecentTransaction recent) {
        this.zaloId = recent.zaloId;
        this.zalopayId = recent.zaloPayId;
        this.zalopayName = recent.zaloPayName;
        this.avatar = recent.avatar;
        this.displayName = recent.displayName;
        this.phoneNumber = recent.phoneNumber;
        this.amount = recent.amount;
        this.message = recent.message;
    }

    public TransferObject(Person person) {
        this.zaloId = person.zaloId;
        this.zalopayId = person.zaloPayId;
        this.zalopayName = person.zalopayname;
        this.avatar = person.avatar;
        this.displayName = person.displayName;
        this.phoneNumber = PhoneUtil.formatPhoneNumber(person.phonenumber);
    }

    protected TransferObject(Parcel in) {
        this.zaloId = in.readLong();
        this.zalopayId = in.readString();
        this.displayName = in.readString();
        this.avatar = in.readString();
        this.zalopayName = in.readString();
        this.phoneNumber = in.readString();
        this.amount = in.readLong();
        this.message = in.readString();
        int tmpTransferMode = in.readInt();
        this.transferMode = tmpTransferMode == -1 ? null : Constants.TransferMode.values()[tmpTransferMode];
        int tmpActivateSource = in.readInt();
        this.activateSource = tmpActivateSource == -1 ? null : Constants.ActivateSource.values()[tmpActivateSource];
    }

    public static final Creator<TransferObject> CREATOR = new Creator<TransferObject>() {
        @Override
        public TransferObject createFromParcel(Parcel source) {
            return new TransferObject(source);
        }

        @Override
        public TransferObject[] newArray(int size) {
            return new TransferObject[size];
        }
    };
}
