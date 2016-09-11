package vn.com.zalopay.game.ui.component.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import timber.log.Timber;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.base.AppGameSingletonLifeCircle;
import vn.com.zalopay.game.businnesslogic.behavior.view.AppGameInjectView;
import vn.com.zalopay.game.businnesslogic.behavior.view.AppGameViewFactory;
import vn.com.zalopay.game.ui.component.fragment.AppGameFragment;
import vn.com.zalopay.game.ui.webview.AppGameWebViewProcessor;

public class AppGameActivity extends AppGameBaseActivity {
    private AppGameFragment mFragment;
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__main);
        mToolbar = (Toolbar) findViewById(R.id.toolbarLayout);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Fragment subView = getView();

        if (subView != null)
            inflatFragment(subView, false);
    }

    /**
     * start flow by app id.
     */
    private Fragment getView() {
        mFragment = AppGameInjectView.getInstance(AppGameViewFactory.procedureChannel()).getView();
        Timber.d("getView fragment [%s]", mFragment);
        return mFragment;
    }

    @Override
    public void onBackPressed() {
        /*
        if(mFragment != null && mFragment.canBack())
        {
            mFragment.goBack();

            return;
        }

        super.onBackPressed();
        */
        Timber.d("onBackPressed hasError [%s]", AppGameWebViewProcessor.hasError);
        if(AppGameWebViewProcessor.hasError)
            super.onBackPressed();
        else
            return;
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleId);
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

}
