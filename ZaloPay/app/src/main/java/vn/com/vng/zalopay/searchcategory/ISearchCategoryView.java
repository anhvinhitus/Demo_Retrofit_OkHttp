package vn.com.vng.zalopay.searchcategory;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import vn.com.vng.zalopay.domain.model.InsideApp;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by khattn on 3/10/17.
 * Search Category interface helps communicate presenter and view
 */

interface ISearchCategoryView extends ILoadDataView {

    void setFindResult(List<InsideApp> apps, List<ZaloFriend> friends, String key);

    void showResultView(boolean noResultView, boolean resultView);

    void refreshInsideApps(List<InsideApp> list);

    void showConfirmDialog(String message, String btnConfirm, String btnCancel, ZPWOnEventConfirmDialogListener listener);
}
