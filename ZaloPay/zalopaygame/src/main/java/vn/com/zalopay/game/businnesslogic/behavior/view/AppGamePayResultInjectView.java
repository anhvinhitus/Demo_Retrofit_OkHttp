package vn.com.zalopay.game.businnesslogic.behavior.view;

import timber.log.Timber;
import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameGetView;
import vn.com.zalopay.game.ui.component.fragment.AppGameFragment;
import vn.com.zalopay.game.ui.component.fragment.FragmentPayResult;

/**
 * injector view result payment view
 */
public class AppGamePayResultInjectView implements IAppGameGetView
{
    @Override
    public AppGameFragment getView() {
        Timber.d("getView start");
        return FragmentPayResult.newInstance();
    }
}
