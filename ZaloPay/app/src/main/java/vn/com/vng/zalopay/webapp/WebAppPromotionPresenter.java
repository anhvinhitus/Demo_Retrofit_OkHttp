package vn.com.vng.zalopay.webapp;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.webapp.framework.ZPWebViewApp;
import vn.com.vng.webapp.framework.ZPWebViewAppProcessor;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.navigation.Navigator;

/**
 * Created by datnt10 on 6/22/17.
 */

class WebAppPromotionPresenter extends AbstractWebAppPresenter<IWebAppPromotionView> {

    @Inject
    WebAppPromotionPresenter(AccountStore.Repository accountRepository, Navigator navigator, AppResourceStore.Repository appResourceRepository, MerchantStore.Repository merchantRepository) {
        super(accountRepository, navigator, appResourceRepository, merchantRepository);
    }

    void initWebView(ZPWebViewApp webView) {
        mWebViewProcessor = new ZPWebViewAppProcessor(webView, mView);
        mWebViewProcessor.registerNativeModule(new WebAppNativeModule(mProcessMessageListener));

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                Timber.d("WebLoading progress: %s", progress);
                if (mView == null) {
                    return;
                }

                mView.updateLoadProgress(progress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (mView == null) {
                    return;
                }

                mView.onReceivedTitle(title);
            }
        });
    }
}
