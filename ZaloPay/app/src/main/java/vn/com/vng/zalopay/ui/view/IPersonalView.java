package vn.com.vng.zalopay.ui.view;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by Duke on 3/27/17.
 */

public interface IPersonalView {
    void setUserInfo(User user);

    void setAvatar(String avatar);

    void setDisplayName(String displayName);

    void setZaloPayName(String zaloPayName);

    void setBalance(long balance);

//    void setBankLinkText(int linkBankStatus, int cardAmount, int accAmount);
    void setBankLinkText(int accounts);

    Context getContext();

    Activity getActivity();

    Fragment getFragment();
}
