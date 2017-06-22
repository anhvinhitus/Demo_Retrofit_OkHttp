package vn.com.vng.zalopay.webapp;

import android.os.Build;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.webapp.framework.ZPWebViewApp;
import vn.com.vng.webapp.framework.ZPWebViewAppProcessor;
import vn.com.vng.zalopay.R;
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
                mView.updateLoadProgress(progress);

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    if (progress == 100) {
                        String title = view.getTitle();
                        setReceivedTitleStatus(view, title);
                    }
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                setReceivedTitleStatus(view, title);
            }
        });
    }

    private void setReceivedTitleStatus(WebView view, String title) {
        if (view.canGoBack()) {
            setHomeDisplayStatus(false);
        } else {
            setHomeDisplayStatus(true);
            title = getActivity().getString(R.string.promotion_title);
        }

        mView.onReceivedTitle(title);
    }

    private void setHomeDisplayStatus(boolean isHome) {
        if (isHome) {
            mView.setHiddenBackButton(true);
            mView.setHiddenShareButton(true);
            mView.setHiddenTabBar(false);
        } else {
            mView.setHiddenBackButton(false);
            mView.setHiddenShareButton(false);
            mView.setHiddenTabBar(true);
        }
    }
}
