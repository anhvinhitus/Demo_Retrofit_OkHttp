package vn.com.vng.zalopay.linkcard.ui;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class TutorialLinkCardActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TutorialLinkCardFragment.newInstance(getIntent().getExtras());
    }
}
