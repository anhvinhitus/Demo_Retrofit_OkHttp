package vn.com.zalopay.game.businnesslogic.behavior.channel;

import android.content.Intent;

import timber.log.Timber;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameStartFlow;
import vn.com.zalopay.game.ui.component.activity.AppGameActivity;

/**
 * Created by admin on 8/27/16.
 */
public class AppGameStartPayGameChannel implements IAppGameStartFlow {
    @Override
    public void startFlow() {
        Timber.d("===starting flow AppGameGlobal.getApplication()[%s]", AppGameGlobal.getApplication());

        if (AppGameGlobal.getOwnerActivity() == null) {
            Timber.e(getClass().getName() + " application is null");
            return;
        }

//        if (AppGameBaseActivity.getCurrentActivity() instanceof AppGameActivity) {
//            Timber.e("there're a running activity");
//            return;
//        }

        Timber.d("start getOwnerActivity [%s]", AppGameGlobal.getOwnerActivity());
        Intent intentZingXu = new Intent(AppGameGlobal.getOwnerActivity(), AppGameActivity.class);
        AppGameGlobal.getOwnerActivity().startActivity(intentZingXu);
        Timber.d("startFlow end");
    }
}
