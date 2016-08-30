package vn.com.zalopay.game.businnesslogic.behavior.channel;

import android.content.Intent;

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
        AppGameGlobal.getLog().d(getClass().getName(),"===starting flow===");

        if(AppGameGlobal.getApplication() == null)
        {
            AppGameGlobal.getLog().e(getClass().getName(),"application is null");
            return;
        }

        if(AppGameBaseActivity.getCurrentActivity() instanceof AppGameActivity)
        {
            AppGameGlobal.getLog().e(getClass().getName(),"there're a running activity");
            return;
        }

        Intent intentZingXu = new Intent(AppGameGlobal.getApplication(), AppGameActivity.class);
        intentZingXu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        AppGameGlobal.getApplication().startActivity(intentZingXu);
    }
}
