package vn.com.vng.zalopay.react.redpacket;

import android.content.Context;
import android.util.Pair;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.react.Helpers;

/**
 * Created by longlv on 16/07/2016.
 * Handle get all zalo friends result and translate to WritableArray for consuming in react native
 */
class GetAllFriendSubscriber extends DefaultSubscriber<WritableArray> {

    private WeakReference<Promise> mPromise;
    private Context mContext;

    GetAllFriendSubscriber(Promise promise, Context context) {
        mPromise = new WeakReference<>(promise);
        this.mContext = context;
    }

    @Override
    public void onError(Throwable e) {
        Timber.w(e, "error while getting all friends");
        if (mPromise.get() == null) {
            return;
        }

        Pair<Integer, String> error = Helpers.createReactError(mContext, e);
        Helpers.promiseResolveError(mPromise.get(), error);
        mPromise.clear();
    }

    @Override
    public void onNext(WritableArray writableArray) {
        Timber.d("receive %s friends", writableArray.size());
        if (mPromise.get() == null) {
            return;
        }

        Helpers.promiseResolveSuccess(mPromise.get(), writableArray);
        mPromise.clear();
    }
}
