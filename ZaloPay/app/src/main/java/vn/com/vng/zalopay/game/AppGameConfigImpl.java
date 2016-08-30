package vn.com.vng.zalopay.game;

import vn.com.vng.zalopay.BuildConfig;
import vn.com.zalopay.game.businnesslogic.provider.config.IGetUrlConfig;

/**
 * Created by admin on 8/30/16.
 */
public class AppGameConfigImpl implements IGetUrlConfig
{
    @Override
    public String getUrl()
    {
        return BuildConfig.HOST;
    }
}
