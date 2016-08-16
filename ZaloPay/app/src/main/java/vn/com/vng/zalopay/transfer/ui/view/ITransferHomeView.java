package vn.com.vng.zalopay.transfer.ui.view;

import java.util.List;

import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by AnhHieu on 8/15/16.
 */
public interface ITransferHomeView extends ILoadDataView {

    void setData(List<RecentTransaction> list);

    void onGetProfileSuccess(Person person);
}
