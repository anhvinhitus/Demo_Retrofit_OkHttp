package vn.com.vng.zalopay.senderror;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class SendErrorActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return SendErrorFragment.newInstance();
    }
}
