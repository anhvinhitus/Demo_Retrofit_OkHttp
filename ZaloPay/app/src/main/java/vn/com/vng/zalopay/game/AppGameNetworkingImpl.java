package vn.com.vng.zalopay.game;

import android.content.Context;

import vn.com.zalopay.game.businnesslogic.provider.networking.INetworking;
import vn.com.zalopay.wallet.utils.ConnectionUtil;

/**
 * Created by admin on 8/30/16.
 */
public class AppGameNetworkingImpl implements INetworking
{
    @Override
    public boolean isOnline(Context pContext) {
        return ConnectionUtil.isOnline(pContext);
    }
}
