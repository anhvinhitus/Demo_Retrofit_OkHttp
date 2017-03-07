package vn.com.zalopay.wallet.business.entity.staticconfig.page;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.base.BaseEntity;

public class DDynamicSelectionViewGroup extends BaseEntity<DDynamicSelectionViewGroup> {
    public int breakLine = 0;
    public boolean isAutoSelect = true;
    public boolean isFilterPmc = true;
    public List<DDynamicSelectionViewItem> items = null;

    public boolean isDefaultBreakLine() {
        return breakLine == 0;
    }
}
