package vn.com.vng.zalopay.feedback;

import android.content.Context;

import javax.inject.Inject;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.UserCollector;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.zalopay.feedback.FeedbackCollector;
import vn.zalopay.feedback.collectors.AppCollector;
import vn.zalopay.feedback.collectors.DeviceCollector;
import vn.zalopay.feedback.collectors.NetworkCollector;

/**
 * Created by hieuvm on 1/6/17.
 */

final class FeedbackPresenter extends AbstractPresenter<IFeedbackView> {

    private Context mContext;
    private UserConfig mUserConfig;

    @Inject
    FeedbackPresenter(Context context, UserConfig userConfig) {
        this.mContext = context;
        this.mUserConfig = userConfig;
    }

    public void sendEmail(String transactionID, String category, String email, String description,
                          boolean user, boolean app, boolean device) {

        FeedbackCollector feedbackCollector = collectInformation(user, app, device);
    }

    private FeedbackCollector collectInformation(boolean user, boolean app, boolean device) {
        FeedbackCollector mCollector = new FeedbackCollector();

        if (user) {
            mCollector.installCollector(new UserCollector(mUserConfig));
        }

        if (app) {
            mCollector.installCollector(new AppCollector(mContext));
        }

        if (device) {
            mCollector.installCollector(new DeviceCollector());
        }
        mCollector.installCollector(new NetworkCollector(mContext));

        return mCollector;
    }

}
