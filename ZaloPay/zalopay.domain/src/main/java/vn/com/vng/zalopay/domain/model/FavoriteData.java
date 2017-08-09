package vn.com.vng.zalopay.domain.model;

import android.text.TextUtils;

/**
 * Created by hieuvm on 7/28/17.
 * *
 */

public class FavoriteData {
    public long zaloId;
    public String phoneNumber;
    public String displayName;
    public String avatar;
    public int status;

    public FavoriteData() {
    }

    public FavoriteData(FavoriteData other) {
        this.zaloId = other.zaloId;
        this.phoneNumber = other.phoneNumber;
        this.displayName = other.displayName;
        this.avatar = other.avatar;
        this.status = other.status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FavoriteData that = (FavoriteData) o;

        return zaloId > 0 && zaloId == that.zaloId || !TextUtils.isEmpty(phoneNumber) && phoneNumber.equals(that.phoneNumber);

    }
}
