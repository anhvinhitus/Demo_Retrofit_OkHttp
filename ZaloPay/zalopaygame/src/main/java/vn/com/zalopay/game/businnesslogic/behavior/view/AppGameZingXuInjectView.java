package vn.com.zalopay.game.businnesslogic.behavior.view;

import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameGetView;
import vn.com.zalopay.game.ui.component.fragment.AppGameFragment;
import vn.com.zalopay.game.ui.component.fragment.FragmentZingXu;

/**
 * injector create zingxu view
 */
public class AppGameZingXuInjectView implements IAppGameGetView
{
    @Override
    public AppGameFragment getView()
    {
        return FragmentZingXu.newInstance();
    }
}
