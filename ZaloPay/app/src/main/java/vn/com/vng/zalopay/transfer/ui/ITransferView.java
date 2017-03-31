package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;

import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.transfer.model.TransferObject;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 13/06/2016.
 * Interface of TransferPresenter
 */
public interface ITransferView extends ILoadDataView {
    Activity getActivity();

    Fragment getFragment();

    void setEnabledTransfer(boolean enabled);

    void showDialogThenClose(String content, String title, int dialogType);

    void setMinMaxMoney(long min, long max);

    long getAmount();

    String getMessage();

    void showErrorTransferFixedMoney(String error);

    void hideErrorTransferFixedMoney();

    void setTransferInfo(TransferObject object, boolean amountDynamic);

    void setUserInfo(Person person);
}
