package vn.com.vng.zalopay.zpsdk;

import android.text.TextUtils;

import timber.log.Timber;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;

/**
 * Created by AnhHieu on 9/10/16.
 * *
 */
public class DefaultZPGatewayInfoCallBack implements ZPWGatewayInfoCallback {
    @Override
    public void onProcessing() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onError(String s) {
        Timber.d("load payment sdk error: %s", TextUtils.isEmpty(s) ? "" : s);
    }

    @Override
    public void onUpVersion(boolean forceUpdate, String latestVersion, String msg) {

    }

}
