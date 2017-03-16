package vn.com.vng.zalopay.searchcategory;

import android.app.Activity;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import vn.com.vng.zalopay.domain.model.InsideApp;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by khattn on 3/10/17.
 * Search Category interface helps communicate presenter and view
 */

public interface ISearchCategoryView extends ILoadDataView {
    Activity getActivity();

    void showSearchResultView(boolean isShow);

    void showNoResultView(boolean isShow);

    void refreshInsideApps(List<InsideApp> list);

    void showConfirmDialog(String message, String btnConfirm, String btnCancel, ZPWOnEventConfirmDialogListener listener);
}
