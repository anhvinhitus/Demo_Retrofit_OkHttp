package vn.com.zalopay.wallet.business.entity.staticconfig.page;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.base.BaseEntity;

public class DStaticViewGroup extends BaseEntity<DStaticViewGroup> {
    public List<DStaticView> ImageView;
    public List<DStaticView> TextView;
    public List<DStaticView> EditText;
}
