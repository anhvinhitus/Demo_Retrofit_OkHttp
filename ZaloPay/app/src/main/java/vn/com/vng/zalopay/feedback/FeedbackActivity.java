package vn.com.vng.zalopay.feedback;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class FeedbackActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return FeedbackFragment.newInstance();
    }
}
