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

    private Promise mPromise;
    private Context mContext;

    GetAllFriendSubscriber(Promise promise, Context context) {
        this.mPromise = promise;
        this.mContext = context;
    }

    @Override
    public void onError(Throwable e) {
        Timber.w(e, "error while getting all friends");
        if (mPromise == null) {
            return;
        }

        Pair<Integer, String> error = Helpers.createReactError(mContext, e);
        Helpers.promiseResolveError(mPromise, error);
        mPromise = null;
    }

    @Override
    public void onNext(WritableArray writableArray) {
        Timber.d("receive %s friends", writableArray.size());
        if (mPromise == null) {
            return;
        }

        Helpers.promiseResolveSuccess(mPromise, writableArray);
        mPromise = null;
    }
}
