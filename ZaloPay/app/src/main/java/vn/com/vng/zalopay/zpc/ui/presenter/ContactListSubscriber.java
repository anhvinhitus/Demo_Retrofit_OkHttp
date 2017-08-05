package vn.com.vng.zalopay.zpc.ui.presenter;

import android.database.Cursor;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.zpc.ui.view.IZaloFriendListView;

/**
 * Contact list subscriber
 * Created by huuhoa on 8/5/17.
 */
class ContactListSubscriber extends DefaultSubscriber<Cursor> {
    private final boolean mIsSearch;
    private final WeakReference<IZaloFriendListView> mViewReference;

    ContactListSubscriber(boolean isSearch, IZaloFriendListView view) {
        mIsSearch = isSearch;
        mViewReference = new WeakReference<>(view);
    }

    @Override
    public void onNext(Cursor cursor) {

        if (cursor == null || cursor.isClosed()) {
            return;
        }

        if (mViewReference.get() == null) {
            return;
        }

        mViewReference.get().swapCursor(cursor);
        mViewReference.get().hideLoading();
        mViewReference.get().setRefreshing(false);
        mViewReference.get().checkIfEmpty();

        if (!mIsSearch) {
            mViewReference.get().setSubTitle(String.format("(%s)", cursor.getCount()));
        }
    }

    @Override
    public void onError(Throwable e) {
        Timber.d(e, "Get friend zalo error");
        if (ResponseHelper.shouldIgnoreError(e)) {
            return;
        }

        if (mViewReference.get() == null) {
            return;
        }

        mViewReference.get().showError(ErrorMessageFactory.create(mViewReference.get().getContext(), e));
        mViewReference.get().setRefreshing(false);
        mViewReference.get().hideLoading();
    }
}
