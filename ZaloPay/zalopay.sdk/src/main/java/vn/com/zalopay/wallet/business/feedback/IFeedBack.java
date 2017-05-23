package vn.com.zalopay.wallet.business.feedback;

import android.app.Activity;

import vn.com.zalopay.feedback.FeedbackCollector;

/**
 * Created by lytm on 27/04/2017.
 */

public interface IFeedBack {
    FeedbackCollector getFeedbackCollector() throws Exception;

    void showDialog(Activity activity) throws Exception;
}
