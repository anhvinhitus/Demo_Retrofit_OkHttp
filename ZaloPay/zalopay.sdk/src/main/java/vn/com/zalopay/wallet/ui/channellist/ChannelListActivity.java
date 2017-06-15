package vn.com.zalopay.wallet.ui.channellist;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.ui.BaseFragment;
import vn.com.zalopay.wallet.ui.ToolbarActivity;

/***
 * payment channel list screen.
 */
public class ChannelListActivity extends ToolbarActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_toolbar;
    }

    @Override
    protected BaseFragment getFragmentToHost() {
        return ChannelListFragment.newInstance();
    }
}
