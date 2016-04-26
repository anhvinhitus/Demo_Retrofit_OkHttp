package vn.com.vng.vmpay.account.listener;

import vn.com.vng.vmpay.account.models.ZaloProfile;

/**
 * Created by longlv on 22/04/2016.
 */
public interface IProfileListener {
    public void onGetProfileSuccess(ZaloProfile zaloProfile);
    public void onGetProfileFail(String msg);

}
