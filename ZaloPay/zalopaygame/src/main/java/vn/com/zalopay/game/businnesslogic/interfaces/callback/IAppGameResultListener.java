package vn.com.zalopay.game.businnesslogic.interfaces.callback;

import vn.com.zalopay.game.businnesslogic.entity.base.AppGameError;

public interface IAppGameResultListener
{
    void onError(AppGameError pError);

    void onLogout();
}
