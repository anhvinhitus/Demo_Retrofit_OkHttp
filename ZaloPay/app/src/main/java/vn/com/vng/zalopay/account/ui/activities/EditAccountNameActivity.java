package vn.com.vng.zalopay.account.ui.activities;

import vn.com.vng.zalopay.account.ui.fragment.EditAccountNameFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 8/12/16.
 */
public class EditAccountNameActivity extends BaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return EditAccountNameFragment.newInstance();
    }
}
