package vn.com.vng.zalopay.ui.view;

import android.content.Context;

import java.util.List;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.menu.model.MenuItem;

/**
 * Created by AnhHieu on 5/11/16.
 */
public interface ILeftMenuView {

    void setUserInfo(User user);

    void setAvatar(String avatar);

    void setDisplayName(String displayName);

    void setZaloPayName(String zaloPayName);

    void setMenuItem(List<MenuItem> var);

    Context getContext();
}
