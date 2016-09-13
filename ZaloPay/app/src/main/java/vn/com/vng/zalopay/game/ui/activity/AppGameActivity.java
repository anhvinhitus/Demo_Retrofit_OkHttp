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
import vn.com.vng.zalopay.game.ui.fragment.FragmentPayGame;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.base.AppGameSingletonLifeCircle;
import vn.com.vng.zalopay.game.ui.fragment.AppGameFragment;

public class AppGameActivity extends AppGameBaseActivity {
    protected AppGameFragment mFragment;
    protected Toolbar mToolbar;

    ImageView mLogoView;
    TextView mTitleView;

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

        Fragment subView = getView();
        if (subView != null)
            inflatFragment(subView, false);
    }

    /**
     * start flow by app id.
     */
    protected Fragment getView() {
        mFragment = FragmentPayGame.newInstance();
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

        AppGameSingletonLifeCircle.disposeAll();
    }

    @Override
    public void logout() {
        Timber.d("logout start");
        if (AppGameGlobal.getResultListener() != null)
            AppGameGlobal.getResultListener().onLogout();

        finish();
    }

    @Override
    public void startUrl(String pUrl) {
        mFragment.loadUrl(pUrl);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitleView.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        mTitleView.setText(titleId);
    }

    public void setLogo(String url) {
        Timber.d("setLogo url %s", url);
        if (getSupportActionBar() != null) {
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

    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

}
