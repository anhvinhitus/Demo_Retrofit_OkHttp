package vn.com.vng.zalopay.ui.view;

import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 5/11/16.
 */
public interface ILeftMenuView {

    void setBalance(long balance);

    void setUserInfo(User user);

    void setAvatar(String avatar);

    void setDisplayName(String displayName);

    void setZaloPayName(String zaloPayName);
}
