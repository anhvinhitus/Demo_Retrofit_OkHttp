package vn.com.zalopay.game.businnesslogic.behavior.channel;

import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameStartFlow;
import vn.com.zalopay.game.ui.component.activity.AppGameActivity;

/**
 * Created by admin on 8/27/16.
 *
 */
public class AppGameChannelFactory {
    public static IAppGameStartFlow procedureChannel() {
        if (AppGameGlobal.isResultChannel()) {
            return new AppGameStartResultChannel();
        } else if (AppGameGlobal.isPayForGameChannel()) {
            return new AppGameStartPayGameChannel();
        } else {
            return null;
        }
    }
}
