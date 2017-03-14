package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 3/26/16.
 * *
 */
public interface IHomeView extends ILoadDataView {
    Activity getActivity();

    void refreshIconFont();
    // datnt10 13.03.2017 add >>
    void setBalance(long balance);
    void setTotalNotify(int total);
    // datnt10 13.03.2017 add <<
}
