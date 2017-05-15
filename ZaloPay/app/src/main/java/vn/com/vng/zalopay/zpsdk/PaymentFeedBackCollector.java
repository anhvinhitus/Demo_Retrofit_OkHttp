package vn.com.vng.zalopay.zpsdk;

import android.app.Activity;
import android.content.Intent;

import vn.com.vng.zalopay.feedback.FeedbackActivity;
import vn.com.zalopay.wallet.business.feedback.IFeedBack;
import vn.zalopay.feedback.FeedbackCollector;

/**
 * Created by hieuvm on 5/12/17.
 * *
 */

public class PaymentFeedBackCollector implements IFeedBack {

    @Override
    public FeedbackCollector getFeedbackCollector() throws Exception {
        return FeedbackCollector.instance();
    }

    @Override
    public void showDialog(Activity activity) throws Exception {
        Intent intent = new Intent(activity, FeedbackActivity.class);
        activity.startActivity(intent);
    }
}
