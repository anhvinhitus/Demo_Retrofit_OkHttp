package vn.com.vng.zalopay.requestsupport;

import java.util.List;

import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by hieuvm on 1/15/17.
 */

interface IChooseCategoryView extends ILoadDataView {
    void setData(List<AppResource> data);
}
