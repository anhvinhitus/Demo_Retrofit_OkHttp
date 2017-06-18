package vn.com.vng.zalopay.data.zalosdk;

import vn.com.vng.zalopay.domain.model.zalosdk.ZaloProfile;

/**
 * Created by hieuvm on 6/13/17.
 * *
 */
public interface IProfileCallback {
    void onGetProfile(ZaloProfile profile);

    void onGetProfileFailure();
}