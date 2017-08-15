package vn.com.vng.zalopay.zpc.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import vn.com.vng.zalopay.BundleConstants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

public class ContactListActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.MONEYTRANSFER_ZFRIEND, -1, ZPEvents.MONEYTRANSFER_ZFRIEND_TOUCH_BACK);
    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.title_sub)
    TextView tvSubTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (tvTitle != null) {
            tvTitle.setText(R.string.title_activity_zalopay_contact);
        }

        // show/hide subTitle
        if (getExtras().containsKey(BundleConstants.PHONE_NUMBER)
                && PhoneUtil.isMobileNumber(getExtras().getString(BundleConstants.PHONE_NUMBER))) {
            tvSubTitle.setVisibility(View.GONE);
        } else {
            tvSubTitle.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return ContactListFragment.newInstance(getExtras());
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

    @Override
    public void setTitle(CharSequence title) {
        if (tvTitle == null) {
            return;
        }

        tvTitle.setText(title);
    }
}
