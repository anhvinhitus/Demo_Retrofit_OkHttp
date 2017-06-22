package vn.com.vng.zalopay.webapp;

import vn.com.vng.webapp.framework.IWebViewListener;
import vn.com.vng.zalopay.ui.presenter.IPaymentDataView;

/**
 * Created by longlv on 2/9/17.
 * *
 */
interface IWebAppView extends IPaymentDataView, IWebViewListener {
    /**
     * Tell view to update progress of loading webview
     *
     * @param progress current progress in scale 0--100
     */
    void updateLoadProgress(int progress);

    void dismissBottomSheet();

    /**
     * Hide error view
     */
    void hideError();

    void onReceivedTitle(String title);

    void setRefreshing(boolean setRefresh);

    void clearCached();
}
