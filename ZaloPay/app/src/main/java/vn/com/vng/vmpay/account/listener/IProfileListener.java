package vn.com.vng.vmpay.account.listener;

import vn.com.vng.vmpay.account.models.Profile;

/**
 * Created by longlv on 22/04/2016.
 */
public interface IProfileListener {
    public void onGetProfileSuccess(Profile profile);
    public void onGetProfileFail(String msg);

}
