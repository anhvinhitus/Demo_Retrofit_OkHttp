package vn.com.vng.zalopay.domain.model;

/**
 * Created by longlv on 10/27/16.
 * Cache user profile level 2
 */

public class ProfileLevel2 {
    public boolean isReceivedOtp;
    public String phoneNumber;
    public String zaloPayName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileLevel2 that = (ProfileLevel2) o;

        if (phoneNumber != null ? !phoneNumber.equals(that.phoneNumber) : that.phoneNumber != null)
            return false;
        return zaloPayName != null ? zaloPayName.equals(that.zaloPayName) : that.zaloPayName == null;

    }

    @Override
    public int hashCode() {
        int result = phoneNumber != null ? phoneNumber.hashCode() : 0;
        result = 31 * result + (zaloPayName != null ? zaloPayName.hashCode() : 0);
        return result;
    }
}
