package vn.com.vng.zalopay.searchcategory;

import com.airbnb.epoxy.SimpleEpoxyModel;

import vn.com.vng.zalopay.R;

/**
 * Created by khattn on 4/17/17.
 * Search home title model
 */

class CommonAppTitleModel extends SimpleEpoxyModel {

    CommonAppTitleModel() {
        super(R.layout.layout_title_search);
    }

    @Override
    public int getSpanSize(int totalSpanCount, int position, int itemCount) {
        return totalSpanCount;
    }
}
