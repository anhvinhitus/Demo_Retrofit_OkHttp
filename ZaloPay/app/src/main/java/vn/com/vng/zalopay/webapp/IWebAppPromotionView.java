package vn.com.vng.zalopay.webapp;

import vn.com.vng.webapp.framework.IWebViewListener;
import vn.com.vng.zalopay.ui.presenter.IPaymentDataView;

/**
 * Created by datnt10 on 6/22/17.
 */

interface IWebAppPromotionView extends IWebAppView {
    void setHiddenBackButton(boolean hide);

    void setHiddenShareButton(boolean hide);

    void setHiddenTabBar(boolean hide);
}
