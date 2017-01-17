package vn.com.vng.zalopay.zalosdk;

import android.content.Context;
import android.os.Looper;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.zalosdk.APICallback;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
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

        Timber.d("ZaloSDKApiImpl: create %s", this);
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
        if (AndroidUtils.isMainThead()) {
            Timber.d("executeNext: %s mRunning %s", mTaskList.size(), mRunning);
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
            AndroidUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    executeNext();
                }
            });
        }
    }

    public void getProfile() {
        ZaloSdkTask task = new ZaloSdkTask(mContext, new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject result) {
                saveUserProfile(result);
                dequeue();
                mRunning = false;

                executeNext();
            }
        });
        enqueue(task);
        executeNext();
    }

    private void saveUserProfile(JSONObject result) {
        Timber.d("saveUserProfile: %s", result);
        if (result != null) {
            mUserConfig.saveZaloUserInfo(result);
        }
    }

    @Override
    public void getFriendList(int pageIndex, int totalCount, final APICallback callback) {
        ZaloSdkTask task = new ZaloSdkTask(mContext, ZaloSdkTask.Action.GET_FRIEND, pageIndex, totalCount, new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject result) {
                handleResult(result, callback);
                dequeue();
                mRunning = false;
                executeNext();
            }
        });

        enqueue(task);
        executeNext();
    }

    private void handleResult(final JSONObject result, final APICallback callback) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            //save data in background
            mThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onResult(result);
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onResult(result);
            }
        }
    }
}
