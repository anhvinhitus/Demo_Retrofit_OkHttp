package vn.com.zalopay.wallet.business.feedback;

import android.app.Activity;

import vn.com.zalopay.feedback.FeedbackCollector;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;

/**
 * Created by lytm on 27/04/2017.
 */

public abstract class BaseFeedBack extends SingletonBase implements IFeedBack {

    protected IFeedBack mFeedBack;

    protected IFeedBack getFeedBackCallback() {
        return mFeedBack;
    }

    public void setFeedBack(IFeedBack pFeedBack) {
        this.mFeedBack = pFeedBack;
    }

    @Override
    public FeedbackCollector getFeedbackCollector() throws Exception {

        if (getFeedBackCallback() == null) {
            throw new RuntimeException("FeedBack is NULL");
        }

        return getFeedBackCallback().getFeedbackCollector();
    }

    @Override
    public void showDialog(Activity activity) throws Exception {
        if (getFeedBackCallback() == null) {
            throw new RuntimeException("FeedBack is NULL");
        }

        getFeedBackCallback().showDialog(activity);
    }
}
