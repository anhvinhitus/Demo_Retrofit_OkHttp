package vn.com.vng.zalopay.game;

import java.util.Timer;

import timber.log.Timber;
import vn.com.zalopay.game.businnesslogic.provider.log.ILog;
import vn.com.zalopay.wallet.utils.Log;

/**
 * Created by admin on 8/30/16.
 */
public class AppGameLogImpl implements ILog
{
    @Override
    public void e(String pTag, String pMessage)
    {
        Timber.e(pTag,pMessage);
    }

    @Override
    public void d(String pTag, String pMessage)
    {
        Timber.d(pTag,pMessage);
    }
}
