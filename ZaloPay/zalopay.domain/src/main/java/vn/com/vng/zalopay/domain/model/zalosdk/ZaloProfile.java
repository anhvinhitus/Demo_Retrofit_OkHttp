package vn.com.vng.zalopay.domain.model.zalosdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hieuvm on 6/13/17.
 * *
 */
/*
class ZaloProfile(val userId: Long, val displayName: String, val avatar: String, val birthDate: Long, val userGender: Int, val userName: String) : Parcelable {



    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ZaloProfile> = object : Parcelable.Creator<ZaloProfile> {
            override fun createFromParcel(source: Parcel): ZaloProfile = ZaloProfile(source)
            override fun newArray(size: Int): Array<ZaloProfile?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString(),
            source.readString(),
            source.readLong(),
            source.readInt(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(userId)
        dest.writeString(displayName)
        dest.writeString(avatar)
        dest.writeLong(birthDate)
        dest.writeInt(userGender)
        dest.writeString(userName)
    }

    fun getGender(): String {
        return if (userGender == 1) "Nam" else "Nữ"
    }
}
*/
public class ZaloProfile implements Parcelable {
    public long userId;
    public String displayName;
    public String avatar;
    public int userGender;
    public String userName;
    public long birthDate;

    public String getGender() {
        return userGender == 1 ? "Nam" : "Nữ";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.userId);
        dest.writeString(this.displayName);
        dest.writeString(this.avatar);
        dest.writeInt(this.userGender);
        dest.writeString(this.userName);
        dest.writeLong(this.birthDate);
    }

    public ZaloProfile(long userId, String displayName, String avatar, long birthDate, int userGender, String userName) {
        this.userId = userId;
        this.displayName = displayName;
        this.avatar = avatar;
        this.userGender = userGender;
        this.userName = userName;
        this.birthDate = birthDate;
    }

    public ZaloProfile() {
    }

    protected ZaloProfile(Parcel in) {
        this.userId = in.readLong();
        this.displayName = in.readString();
        this.avatar = in.readString();
        this.userGender = in.readInt();
        this.userName = in.readString();
        this.birthDate = in.readLong();
    }

    public static final Creator<ZaloProfile> CREATOR = new Creator<ZaloProfile>() {
        @Override
        public ZaloProfile createFromParcel(Parcel source) {
            return new ZaloProfile(source);
        }

        @Override
        public ZaloProfile[] newArray(int size) {
            return new ZaloProfile[size];
        }
    };

    public long getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getUserGender() {
        return userGender;
    }

    public String getUserName() {
        return userName;
    }

    public long getBirthDate() {
        return birthDate;
    }
}