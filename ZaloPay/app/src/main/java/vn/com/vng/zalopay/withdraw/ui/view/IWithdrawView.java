package vn.com.vng.zalopay.withdraw.ui.view;

import android.support.v4.app.Fragment;
import android.view.View;

import java.util.List;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 11/08/2016.
 */
public interface IWithdrawView extends ILoadDataView {

    Fragment getFragment();

    void setBalance(long balance);

    void addDenominationMoney(List<Long> val);

    void finish(int result);

    void showEnoughView(boolean isShow);

    void showVisibleStubView();

    void initStubView(View view);
}
