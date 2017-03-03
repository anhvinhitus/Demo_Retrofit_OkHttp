package vn.com.zalopay.wallet.business.entity.staticconfig;

import vn.com.zalopay.wallet.business.entity.base.BaseEntity;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;

public class DPage extends BaseEntity<DPage> {
    public String pageName = null;
    public DStaticViewGroup staticView;
    public DDynamicViewGroup dynamicView;
}
