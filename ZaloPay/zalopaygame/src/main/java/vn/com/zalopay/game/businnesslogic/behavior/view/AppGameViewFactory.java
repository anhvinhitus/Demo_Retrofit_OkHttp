package vn.com.zalopay.game.businnesslogic.behavior.view;

import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameGetView;

/**
 * factory create injector
 */
public class AppGameViewFactory
{
    public static IAppGameGetView procedureChannel()
    {
        if(AppGameGlobal.isResultChannel())
            return new AppGamePayResultInjectView();
        else if(AppGameGlobal.isZingXuChannel())
            return new AppGameZingXuInjectView();
        else if(AppGameGlobal.isPayForGameChannel())
            return new AppGamePayGameInjectView();
        else
            return null;
    }
}
