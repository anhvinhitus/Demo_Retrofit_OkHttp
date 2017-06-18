package vn.com.vng.zalopay.data.zalosdk;

import android.support.annotation.Nullable;

/**
 * Created by hieuvm on 1/17/17.
 */

public interface ZaloSdkApi {

    void getProfile();

    void getProfile(@Nullable IProfileCallback callback);

    void getFriendList(int pageIndex, int totalCount, APICallback callback);
}
