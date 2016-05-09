package vn.com.vng.zalopay.ui.view;

import vn.com.vng.zalopay.domain.model.Order;

/**
 * Created by longlv on 09/05/2016.
 */
public interface IProductDetailView extends LoadDataView {
    public void showOrderDetail(Order order);
}
