package vn.com.vng.zalopay.ui.view;

import java.util.List;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 5/9/16.
 */
public interface IZaloPayView {
    void insertApps(List<AppResource> list);

    void setTotalNotify(int total);

    void showNetworkError();

    void setBalance(long balance);
}
