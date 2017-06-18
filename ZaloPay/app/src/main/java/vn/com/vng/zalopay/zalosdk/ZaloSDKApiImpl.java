package vn.com.vng.zalopay.zalosdk;

import android.content.Context;
import android.os.Looper;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.zalosdk.APICallback;
import vn.com.vng.zalopay.data.zalosdk.IProfileCallback;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.model.zalosdk.ZaloProfile;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by hieuvm on 1/17/17.
 */

public class ZaloSDKApiImpl implements ZaloSdkApi {

    private final Context mContext;
    private final UserConfig mUserConfig;
    private final Queue<ZaloSdkTask> mTaskList;
    private boolean mRunning;
    private final ThreadExecutor mThreadExecutor;

    public ZaloSDKApiImpl(Context context, UserConfig userConfig,
                          ThreadExecutor threadExecutor) {
        this.mContext = context;
        this.mUserConfig = userConfig;
        this.mTaskList = new LinkedList<>();
        this.mThreadExecutor = threadExecutor;
    }

    private void dequeue() {
        mTaskList.poll();
    }

    private void enqueue(ZaloSdkTask task) {
        Timber.d("enqueue tasks size %s", mTaskList.size());

        if (mTaskList.contains(task)) {
            return;
        }

        mTaskList.add(task);
    }

    private ZaloSdkTask peek() {
        return mTaskList.peek();
    }

    private void executeNext() {
        if (AndroidUtils.isMainThread()) {
            Timber.d("executeNext: queue size [%s] mRunning [%s]", mTaskList.size(), mRunning);
            if (mTaskList.size() == 0) {
                return;
            }

            if (mRunning) {
                return;
            }

            mRunning = true;

            ZaloSdkTask task = peek();
            task.execute();
        } else {
            AndroidUtils.runOnUIThread(this::executeNext);
        }
    }

    @Override
    public void getProfile(IProfileCallback callback) {
        ZaloSdkTask task = new ZaloSdkTask(mContext, result -> {
            ZaloProfile profile = transform(result);

            if (profile != null) {
                mUserConfig.saveUserInfo(profile.getUserId(), profile.getAvatar(), profile.getDisplayName(), profile.getBirthDate(), profile.getUserGender(), profile.getUserName());

                if (callback != null) {
                    callback.onGetProfile(profile);
                }
            } else {

                if (callback != null) {
                    callback.onGetProfileFailure();
                }
            }

            dequeue();
            mRunning = false;

            executeNext();
        });
        enqueue(task);
        executeNext();
    }

    @Override
    public void getProfile() {
        getProfile(null);
    }

    private ZaloProfile transform(JSONObject profile) {
        JSONObject result = profile.optJSONObject("result");

        if (result == null) {
            Timber.w("transform zalo profile error : result is NULL");
            return null;
        }

        long zaloId = result.optLong("userId");
        String displayName = result.optString("displayName");
        String avatar = result.optString("largeAvatar");
        long birthday = result.optLong("birthDate");
        int userGender = result.optInt("userGender");
        String userName = result.optString("userName");

        return new ZaloProfile(zaloId, displayName, avatar, birthday, userGender, userName);
    }

    @Override
    public void getFriendList(int pageIndex, int totalCount, final APICallback callback) {
        ZaloSdkTask task = new ZaloSdkTask(mContext, ZaloSdkTask.Action.GET_FRIEND, pageIndex, totalCount, result -> {
            handleResult(result, callback);
            dequeue();
            mRunning = false;
            executeNext();
        });

        enqueue(task);
        executeNext();
    }

    private void handleResult(final JSONObject result, final APICallback callback) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            //save data in background
            mThreadExecutor.execute(() -> {
                if (callback != null) {
                    callback.onResult(result);
                }
            });
        } else {
            if (callback != null) {
                callback.onResult(result);
            }
        }
    }
}
