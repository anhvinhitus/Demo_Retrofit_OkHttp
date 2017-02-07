package vn.com.vng.zalopay.withdraw.ui.view;

import android.app.Activity;

import java.util.List;

import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;

/**
 * Created by longlv on 11/08/2016.
 *
 */
public interface IWithdrawConditionView extends ILoadDataView {
    Activity getActivity();

    void setProfileValid(boolean isValid);

    void refreshListCardSupport(List<BankConfig> list);

    void hideLinkCardNote();

    void hideLinkAccountNote();

    void showConfirmDialog(String message, ZPWOnEventConfirmDialogListener listener);
}
