package vn.com.zalopay.game.businnesslogic.behavior.channel;

import android.content.Intent;

import timber.log.Timber;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameStartFlow;
import vn.com.zalopay.game.ui.component.activity.AppGameActivity;
import vn.com.zalopay.game.ui.component.activity.AppGameBaseActivity;

/**
 * Created by admin on 8/27/16.
 */
public class AppGameStartZingXuChannel implements IAppGameStartFlow
{
    @Override
    public void startFlow()
    {
        Timber.d(getClass().getName() + "===starting flow===");

        if(AppGameGlobal.getApplication() == null)
        {
            Timber.e(getClass().getName() + "===application is null===");
            return;
        }

        if(AppGameBaseActivity.getCurrentActivity() instanceof AppGameActivity)
        {
            Timber.e(getClass().getName()+"===there're a running activity===");
            return;
        }

        Intent intentZingXu = new Intent(AppGameGlobal.getOwnerActivity(), AppGameActivity.class);
        AppGameGlobal.getOwnerActivity().startActivity(intentZingXu);
    }
}
