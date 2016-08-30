package vn.com.zalopay.game.ui.component.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.base.AppGameSingletonLifeCircle;
import vn.com.zalopay.game.businnesslogic.behavior.view.AppGameInjectView;
import vn.com.zalopay.game.businnesslogic.behavior.view.AppGameViewFactory;
import vn.com.zalopay.game.ui.component.fragment.AppGameFragment;

public class AppGameActivity extends AppGameBaseActivity
{

    private AppGameFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__main);

        Fragment subView = getView();

        if(subView != null)
            inflatFragment(subView,false);
    }
    /**
     * start flow by app id.
     */
    private Fragment getView()
    {
        mFragment = AppGameInjectView.getInstance(AppGameViewFactory.procedureChannel()).getView();

        return mFragment;
    }

    @Override
    public void onBackPressed()
    {
        /*
        if(mFragment != null && mFragment.canBack())
        {
            mFragment.goBack();

            return;
        }

        super.onBackPressed();
        */
        return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AppGameSingletonLifeCircle.disposeAll();
    }

    @Override
    public void logout()
    {
        if(AppGameGlobal.getResultListener() != null)
            AppGameGlobal.getResultListener().onLogout();

        finish();
    }

    @Override
    public void startUrl(String pUrl)
    {
        mFragment.loadUrl(pUrl);
    }

}
