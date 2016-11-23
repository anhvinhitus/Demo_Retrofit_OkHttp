package vn.com.vng.zalopay.service;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.NewSessionEvent;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by hieuvm on 11/23/16.
 */

public class UserSession {
    private Context mContext;
    private User mUser;
    private EventBus mEventBus;
    private UserConfig mUserConfig;

    @Inject
    public UserSession(Context context, User user, UserConfig mUserConfig, EventBus eventBus) {
        this.mContext = context;
        this.mUser = user;
        this.mEventBus = eventBus;
        this.mUserConfig = mUserConfig;
    }

    public void beginSession() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    public void endSession() {
        mEventBus.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSessionChange(NewSessionEvent event) {
        Timber.d("accesstoken from server: %s", event.newSession);
        if (!mUser.accesstoken.equals(event.newSession)) {
            Timber.d("Update accesstoken old:  %s", mUser.accesstoken);
            mUser.accesstoken = event.newSession;
            mUserConfig.setAccessToken(event.newSession);
        } else {
            Timber.d("Accesstoken equal accesstoken in client");
        }
    }

}
