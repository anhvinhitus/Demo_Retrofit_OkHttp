package vn.com.vng.zalopay.account.listener;

import vn.com.vng.zalopay.account.models.ZaloProfile;

/**
 * Created by longlv on 22/04/2016.
 */
public interface IProfileListener {
    public void onGetProfileSuccess(ZaloProfile zaloProfile);
    public void onGetProfileFail(String msg);

}
