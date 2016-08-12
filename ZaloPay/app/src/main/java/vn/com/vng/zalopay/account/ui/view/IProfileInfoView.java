package vn.com.vng.zalopay.account.ui.view;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 19/05/2016.
 */
public interface IProfileInfoView extends ILoadDataView {
    void updateUserInfo(User user);

    void setBalance(long balance);
}

