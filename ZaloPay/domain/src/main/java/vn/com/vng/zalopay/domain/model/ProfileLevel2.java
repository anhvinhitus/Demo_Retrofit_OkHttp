package vn.com.vng.zalopay.domain.model;

/**
 * Created by longlv on 10/27/16.
 * Cache user profile level 2
 */

public class ProfileLevel2 {
    public boolean isReceivedOtp;
    public String phoneNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileLevel2 that = (ProfileLevel2) o;

        return phoneNumber != null ? phoneNumber.equals(that.phoneNumber) : that.phoneNumber == null;

    }

    @Override
    public int hashCode() {
        return phoneNumber != null ? phoneNumber.hashCode() : 0;
    }
}
