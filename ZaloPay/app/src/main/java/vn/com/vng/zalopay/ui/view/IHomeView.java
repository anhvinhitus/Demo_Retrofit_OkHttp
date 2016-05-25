package vn.com.vng.zalopay.ui.view;

/**
 * Created by AnhHieu on 3/26/16.
 */
public interface IHomeView extends ILoadDataView {
    void checkIfEmpty();
    void setRefreshing(boolean val);
}
