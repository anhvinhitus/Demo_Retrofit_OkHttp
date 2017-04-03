package vn.com.vng.zalopay.webview.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class WebViewActivity extends BaseToolBarActivity {


    @BindView(R.id.iv_logo)
    SimpleDraweeView mLogoView;

    @BindView(R.id.title)
    TextView mTitleView;

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
        Timber.d("onCreate start");
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
        Timber.d("setTitleAndLogo url %s", url);
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
