package vn.com.zalopay.wallet.business.feedback;

import android.app.Activity;

/**
 * Created by lytm on 27/04/2017.
 */

public interface IFeedBack {
    vn.zalopay.feedback.FeedbackCollector getFeedbackCollector() throws Exception;

    void showDialog(Activity activity) throws Exception;
}
