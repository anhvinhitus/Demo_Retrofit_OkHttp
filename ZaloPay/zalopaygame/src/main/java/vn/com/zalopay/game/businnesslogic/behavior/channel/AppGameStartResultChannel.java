package vn.com.zalopay.game.businnesslogic.behavior.channel;

import android.content.Intent;

import timber.log.Timber;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameStartFlow;
import vn.com.zalopay.game.config.AppGameConfig;
import vn.com.zalopay.game.ui.component.activity.AppGameActivity;
import vn.com.zalopay.game.ui.component.activity.AppGameBaseActivity;

/**
 * Created by admin on 8/27/16.
 */
public class AppGameStartResultChannel implements IAppGameStartFlow {
    @Override
    public void startFlow() {
        Timber.d(getClass().getName() + "===starting flow===");

        //still have a running activity.
        if (AppGameBaseActivity.getCurrentActivity() instanceof AppGameActivity)
            ((AppGameActivity) AppGameActivity.getCurrentActivity()).
                    startUrl(String.format(AppGameConfig.PAY_RESULT_PAGE, AppGameGlobal.getAppGamePayInfo().getApptransid()));

        else {
            //start new activity
            Intent intentPayResult = new Intent(AppGameGlobal.getOwnerActivity(), AppGameActivity.class);
            AppGameGlobal.getOwnerActivity().startActivity(intentPayResult);
        }
    }
}
