package vn.com.vng.zalopay.passport;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by hieuvm on 6/25/17.
 * *
 */

class AbstractLoginPresenter<View extends AbstractLoginView> extends AbstractPresenter<View> {

    private boolean mIsCallingExternal = false;

    void sendResultSuccess(Activity activity) {
        Intent oldIntent = activity.getIntent();
        if (oldIntent == null) {
            activity.finish();
            return;
        }

        Intent intent = new Intent();
        if (oldIntent.getData() != null) {
            intent.setData(oldIntent.getData());
        }

        try {
            PendingIntent pi = oldIntent.getParcelableExtra("pendingResult");
            if (pi != null) {
                pi.send(activity, Activity.RESULT_OK, intent);
            } else {
                activity.setResult(Activity.RESULT_OK, intent);
            }
        } catch (Exception e) {
            Timber.d(e);
        } finally {
            activity.finish();
        }

    }

    void onAuthenticated(User user) {
        Timber.d("login success [accesstoken: %s zalopayid: %s]", user.accesstoken, user.zaloPayId);
        if (mView == null) {
            Timber.w("View login screen is NULL");
            return;
        }

        Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));
        AndroidApplication.instance().createUserComponent(user);

        if (mIsCallingExternal) {
            sendResultSuccess((Activity) mView.getContext());
            return;
        }

        ZPAnalytics.trackEvent(ZPEvents.APPLAUNCHHOMEFROMLOGIN);
        mView.hideLoading();
        mView.gotoHomePage();
        mView.finish();
    }

    void handleIntent(Intent data) {
        if (data == null) {
            return;
        }

        mIsCallingExternal = data.getBooleanExtra("callingExternal", false);
        Timber.d("handleIntent: %s", mIsCallingExternal);
    }


}
