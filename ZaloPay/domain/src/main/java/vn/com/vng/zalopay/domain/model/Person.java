package vn.com.vng.zalopay.domain.model;

import android.text.TextUtils;

/**
 * Created by AnhHieu on 3/25/16.
 * Person model
 */
public class Person {
    private boolean mHasValidZaloPayId = false;

    public final String zaloPayId;

    public String zalopayname;
    public long zaloId;

    public String displayName;
    public String avatar;
    public long birthDate = 0;
    public int userGender = 1;
    public long phonenumber;

    public long status;

    public Person(String zaloPayId) {
        this.zaloPayId = zaloPayId;
        validateZaloPayId();
    }

    public Person(String zaloPayId, String displayName, String avatar) {
        this.zaloPayId = zaloPayId;
        this.displayName = displayName;
        this.avatar = avatar;
        validateZaloPayId();
    }

    public Person(String zaloPayId, String displayName, String avatar, long birthDate, int userGender) {
        this.zaloPayId = zaloPayId;
        this.displayName = displayName;
        this.avatar = avatar;
        this.birthDate = birthDate;
        this.userGender = userGender;
        validateZaloPayId();
    }

    public String getGender() {
        return userGender == 1 ? "Nam" : "Nữ";
    }

    /**
     * @return true if has valid zaloPayId, i.e. non-empty, is number, and larger than zero
     */
    public boolean hasZaloPayId() {
        return mHasValidZaloPayId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Person && zaloPayId.equals(((Person) o).zaloPayId) && !TextUtils.isEmpty(zaloPayId);
    }

    @Override
    public String toString() {
        return "Person {" +
                "zaloPayId='" + zaloPayId + '\'' +
                ", zalopayname='" + zalopayname + '\'' +
                ", displayName='" + displayName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", phonenumber=" + phonenumber +
                '}';
    }

    private void validateZaloPayId() {
        mHasValidZaloPayId = !TextUtils.isEmpty(this.zaloPayId);

        if (mHasValidZaloPayId) {
            try {
                long value = Long.parseLong(this.zaloPayId);
                mHasValidZaloPayId = value > 0;
            } catch (NumberFormatException e) {
                mHasValidZaloPayId = false;
            }
        }
    }
}
