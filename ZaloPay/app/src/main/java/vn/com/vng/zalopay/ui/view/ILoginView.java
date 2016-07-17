package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

/**
 * Created by AnhHieu on 3/26/16.
 */
public interface ILoginView extends ILoadDataView {
    void gotoMainActivity();

    void gotoUpdateProfileLevel2();

    Activity getActivity();

    void gotoInvitationCode();
}
