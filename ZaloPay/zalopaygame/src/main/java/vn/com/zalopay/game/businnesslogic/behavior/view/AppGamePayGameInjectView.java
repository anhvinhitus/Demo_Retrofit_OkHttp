package vn.com.zalopay.game.businnesslogic.behavior.view;

import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameGetView;
import vn.com.zalopay.game.ui.component.fragment.AppGameFragment;
import vn.com.zalopay.game.ui.component.fragment.FragmentPayGame;

/**
 * Created by admin on 8/27/16.
 */
public class AppGamePayGameInjectView implements IAppGameGetView
{
    @Override
    public AppGameFragment getView() {
        return FragmentPayGame.newInstance();
    }
}
