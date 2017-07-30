package vn.com.vng.zalopay.webview.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

public class WebViewActivity extends UserBaseToolBarActivity {

    @BindView(R.id.iv_logo)
    SimpleDraweeView mLogoView;

    @BindView(R.id.title)
    TextView mTitleView;

    private final ActivityTracker mActivityTracker = new ActivityTracker("", -1, -1);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return WebViewFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.webapp_activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitleView.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        mTitleView.setText(titleId);
    }

    public void setTitleAndLogo(String title, String url) {
        Timber.d("setTitleAndLogo : title %s url %s", title, url);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }

        if (TextUtils.isEmpty(url)) {
            mLogoView.setVisibility(View.GONE);
        } else {
            mLogoView.setVisibility(View.VISIBLE);
            mLogoView.setImageURI(url);
        }
    }
}
