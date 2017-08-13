package vn.com.vng.zalopay.zpc.ui.presenter;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.zpc.ui.view.IZaloFriendListView;

/**
 * Created by hieuvm on 8/13/17.
 * *
 */

final class GetUserInfoByPhoneSubscriber extends DefaultSubscriber<ZPProfile> {

    private final WeakReference<IZaloFriendListView> mWeakReferenceView;

    GetUserInfoByPhoneSubscriber(@NonNull IZaloFriendListView view) {
        mWeakReferenceView = new WeakReference<>(view);
    }

    @Override
    public void onStart() {
        IZaloFriendListView mView = mWeakReferenceView.get();

        if (mView != null) {
            mView.showLoading();
        }
    }

    @Override
    public void onNext(ZPProfile profile) {
        IZaloFriendListView mView = mWeakReferenceView.get();

        if (mView == null) {
            return;
        }

        mView.hideLoading();

        if (profile.isDataValid) {
            mView.setProfileNotInZPC(profile);
        } else {
            mView.showDefaultProfileNotInZPC(profile);
        }
    }
}
