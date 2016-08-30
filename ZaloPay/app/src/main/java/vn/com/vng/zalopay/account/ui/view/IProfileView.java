package vn.com.vng.zalopay.account.ui.view;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 19/05/2016.
 */
public interface IProfileView extends ILoadDataView {

    void updateUserInfo(User user);

    void showHideChangePinView(boolean isShow);

    void setZaloPayName(String zaloPayName);
}

