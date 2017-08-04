package vn.com.vng.zalopay.zpc.ui.activity;

import android.support.annotation.NonNull;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.zpc.ui.fragment.ZaloPayContactListFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

import android.os.Bundle;
import android.widget.TextView;

public class ZaloPayContactActivity extends UserBaseToolBarActivity {
    @BindView(R.id.title)
    TextView tvTitle;

    @BindView(R.id.title_sub)
    TextView tvSubTitle;

    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.MONEYTRANSFER_ZFRIEND, -1, ZPEvents.MONEYTRANSFER_ZFRIEND_TOUCH_BACK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (tvTitle == null) {
            return;
        }
        tvTitle.setText(R.string.title_activity_zalopay_contact);
    }

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return ZaloPayContactListFragment.newInstance(getExtras());
    }

    private Bundle getExtras() {
        if (getIntent() == null || getIntent().getExtras() == null) {
            return new Bundle();
        }
        return getIntent().getExtras();
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_zp_contact_list;
    }

    @Override
    public void setSubTitle(String subTitle) {
        if (tvSubTitle == null) {
            return;
        }
        tvSubTitle.setText(subTitle);
    }
}
