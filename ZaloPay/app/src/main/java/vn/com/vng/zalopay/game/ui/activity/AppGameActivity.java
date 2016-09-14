package vn.com.vng.zalopay.game.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import timber.log.Timber;
import vn.com.vng.zalopay.game.ui.fragment.AppGameFragment;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.game.R;

public class AppGameActivity extends BaseActivity {
    protected AppGameFragment mFragment;
    protected Toolbar mToolbar;

    ImageView mLogoView;
    TextView mTitleView;

    @Override
    public BaseFragment getFragmentToHost() {
        return AppGameFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webapp_activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbarLayout);
        setSupportActionBar(mToolbar);
        mLogoView = (ImageView) mToolbar.findViewById(R.id.iv_logo);
        mTitleView = (TextView) mToolbar.findViewById(R.id.title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    /**
     * start flow by app id.
     */
    protected Fragment getView() {
        mFragment = AppGameFragment.newInstance(getIntent().getExtras());
        Timber.d("getView fragment [%s]", mFragment);
        return mFragment;
    }

    @Override
    public void onBackPressed() {
        Timber.d("onBackPressed");
        if (mFragment != null && mFragment.onBackPressed()) {
            // fragment handle the back event
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        Timber.d("onDestroy start");
        super.onDestroy();
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
        if (getSupportActionBar() == null) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            mLogoView.setVisibility(View.GONE);
        } else {
            mLogoView.setVisibility(View.VISIBLE);
            Glide.with(this).load(url)
                    .centerCrop()
                    .placeholder(R.color.silver)
                    .error(R.color.silver)
                    .into(mLogoView);
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

}
