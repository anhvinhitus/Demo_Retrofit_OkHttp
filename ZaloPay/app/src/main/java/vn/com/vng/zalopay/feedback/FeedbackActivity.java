package vn.com.vng.zalopay.feedback;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.vng.zalopay.utils.AndroidUtils;

public class FeedbackActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker("", -1, -1);
    private FeedbackFragment feedbackFragment;

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        feedbackFragment = FeedbackFragment.newInstance();
        return feedbackFragment;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && getCurrentFocus() != null) {
            View v = getCurrentFocus();
            boolean beforeDispatch = AndroidUtils.isKeyboardShowed(v.getRootView());
            boolean ret = super.dispatchTouchEvent(event);
            if (v instanceof EditText) {
                boolean isAfterDispatch = AndroidUtils.isKeyboardShowed(v.getRootView());

                if (event.getAction() == MotionEvent.ACTION_DOWN && isAfterDispatch && beforeDispatch) {
                    AndroidUtils.hideKeyboard(getWindow().getCurrentFocus());
                }
            }
            return ret;
        } else {
            return super.dispatchTouchEvent(event);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (feedbackFragment != null)
            feedbackFragment.onActivityResult(requestCode, resultCode, data);
    }
}
