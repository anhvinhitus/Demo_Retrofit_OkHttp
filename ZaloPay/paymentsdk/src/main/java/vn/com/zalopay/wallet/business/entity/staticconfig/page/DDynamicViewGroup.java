package vn.com.zalopay.wallet.business.entity.staticconfig.page;

import java.util.List;
import java.util.Map;

import vn.com.zalopay.wallet.business.entity.base.BaseEntity;

public class DDynamicViewGroup extends BaseEntity<DDynamicViewGroup> {
    public DDynamicSelectionViewGroup SelectionView;
    public List<DDynamicEditText> EditText;
    public Map<String, Boolean> View;
}
