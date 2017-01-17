package vn.com.vng.zalopay.data.zalosdk;

/**
 * Created by hieuvm on 1/17/17.
 */

public interface ZaloSdkApi {
    void getProfile();
    void getFriendList(int pageIndex, int totalCount, APICallback callback);
}
