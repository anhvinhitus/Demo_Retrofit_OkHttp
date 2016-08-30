package vn.com.zalopay.game.businnesslogic.behavior.view;

import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameGetView;
import vn.com.zalopay.game.ui.component.fragment.AppGameFragment;

public class AppGameInjectView extends AppGameBaseInjectView
{
    private IAppGameGetView mAppGameView;

    private static AppGameInjectView _object;

    public static AppGameInjectView getInstance(IAppGameGetView pAppGameView)
    {
        if(AppGameInjectView._object == null)
            AppGameInjectView._object = new AppGameInjectView(pAppGameView);

        return AppGameInjectView._object;
    }

    public AppGameInjectView(IAppGameGetView pAppGameView)
    {
        this.mAppGameView = pAppGameView;
    }

    @Override
    public AppGameFragment getView()
    {
        if(mAppGameView != null)
            return mAppGameView.getView();
        return null;
    }
}
