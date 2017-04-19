package vn.com.vng.zalopay.feedback;

import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;

public class FeedbackActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return FeedbackFragment.newInstance();
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

}
