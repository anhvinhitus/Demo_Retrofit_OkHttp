package vn.com.zalopay.game.businnesslogic.behavior.view;

import timber.log.Timber;
import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameGetView;
import vn.com.zalopay.game.ui.component.fragment.AppGameFragment;

public class AppGameInjectView extends AppGameBaseInjectView {
    private IAppGameGetView mAppGameView;

    private static AppGameInjectView _object;

    public static AppGameInjectView getInstance(IAppGameGetView pAppGameView) {
        return new AppGameInjectView(pAppGameView);
    }

    public AppGameInjectView(IAppGameGetView pAppGameView) {
        this.mAppGameView = pAppGameView;
    }

    @Override
    public AppGameFragment getView() {
        if (mAppGameView != null)
            return mAppGameView.getView();
        return null;
    }
}
