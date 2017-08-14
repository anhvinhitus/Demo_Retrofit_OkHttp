package vn.com.vng.zalopay.zpc.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.vng.zalopay.zpc.ui.fragment.ZaloPayContactListFragment;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

public class ZaloPayContactActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.MONEYTRANSFER_ZFRIEND, -1, ZPEvents.MONEYTRANSFER_ZFRIEND_TOUCH_BACK);
    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.title_sub)
    TextView tvSubTitle;

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

        // check subTitle & show/hide
        if (TextUtils.isEmpty(subTitle)) {
            tvSubTitle.setVisibility(View.GONE);
        } else {
            tvSubTitle.setVisibility(View.VISIBLE);
            tvSubTitle.setText(subTitle);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (tvTitle == null) {
            return;
        }

        tvTitle.setText(title);
    }
}
