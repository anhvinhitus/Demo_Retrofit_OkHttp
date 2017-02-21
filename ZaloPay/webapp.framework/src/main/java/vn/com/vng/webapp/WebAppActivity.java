//package vn.com.vng.webapp;
//
//import android.content.Context;
//import android.content.res.ColorStateList;
//import android.graphics.PorterDuff;
//import android.os.Build;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.TypedValue;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.Window;
//import android.webkit.WebChromeClient;
//import android.webkit.WebView;
//import android.widget.FrameLayout;
//import android.widget.ProgressBar;
//
//import timber.log.Timber;
//import vn.com.vng.webapp.framework.IWebViewListener;
//import vn.com.vng.webapp.framework.R;
//import vn.com.vng.webapp.framework.ZPWebViewApp;
//import vn.com.vng.webapp.framework.ZPWebViewAppProcessor;
//
//public class WebAppActivity extends AppCompatActivity {
//
//    private ZPWebViewAppProcessor mWebViewProcessor;
//    private ProgressBar mProgressBar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_web_app);
//
//        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
//        initWebView();
//
//        mWebViewProcessor.start("https://zpdemo.github.io/demo-app/vibrate.html", this);
////        mWebViewProcessor.start("http://news.zing.vn/", this);
//    }
//
//    private void initWebView() {
//        ZPWebViewApp webView = (ZPWebViewApp) findViewById(R.id.webview);
//        mWebViewProcessor = new ZPWebViewAppProcessor(webView, new WebViewListener());
//
//        webView.setWebChromeClient(new WebChromeClient() {
//            public void onProgressChanged(WebView view, int progress) {
//                Timber.d("WebLoading progress: %s", progress);
//                if(progress < 100 && mProgressBar.getVisibility() == ProgressBar.GONE) {
//                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
//                }
//
//                mProgressBar.setProgress(progress);
//                if (progress >= 100) {
//                    mProgressBar.setVisibility(ProgressBar.GONE);
//                }
//            }
//
//            @Override
//            public void onReceivedTitle(WebView view, String title) {
//                super.onReceivedTitle(view, title);
//                setTitle(title);
//            }
//        });
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//            if (mWebViewProcessor.onBackPress()) {
//                return true;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//
//    private class WebViewListener implements IWebViewListener {
//        @Override
//        public void finishActivity() {
//            WebAppActivity.this.finish();
//        }
//
//        @Override
//        public void showError(int errorCode) {
//
//        }
//
//        @Override
//        public void showLoading() {
//
//        }
//
//        @Override
//        public void hideLoading() {
//
//        }
//
//        @Override
//        public Context getContext() {
//            return WebAppActivity.this;
//        }
//
//        @Override
//        public void onReceivedTitle(String title) {
//            setTitle(title);
//        }
//    }
//}
