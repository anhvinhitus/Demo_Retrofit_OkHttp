package vn.com.zalopay.game.ui.component.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.webkit.ValueCallback;

import timber.log.Timber;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.IDialogListener;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.ui.component.activity.AppGameBaseActivity;
import vn.com.zalopay.game.ui.webview.AppGameWebView;
import vn.com.zalopay.game.ui.webview.AppGameWebViewProcessor;

/**
 * Created by chucvv on 8/28/16.
 * Fragment
 */
public abstract class AppGameFragment extends Fragment {
    protected AppGameWebView mWebview;
    protected AppGameWebViewProcessor mWebViewProcessor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Timber.d("onViewCreated start");
        initView(view);
        initData();

        super.onViewCreated(view, savedInstanceState);
    }

    public boolean canBack() {
        boolean canBack = false;

        if (mWebview != null) {
            canBack = mWebview.canGoBack();
        }

        return canBack;
    }

    public void goBack() {
        if (mWebview != null)
            mWebview.goBack();
    }

    public void loadUrl(final String pUrl) {
        if (mWebViewProcessor != null)
            mWebViewProcessor.start(pUrl, getActivity(), new ITimeoutLoadingListener() {
                @Override
                public void onTimeoutLoading() {
                    Timber.d("onProgressTimeout-%s", pUrl);
                    //load website timeout, show confirm dialog: continue to load or exit.
                    if (AppGameGlobal.getDialog() != null)
                        AppGameGlobal.getDialog().showConfirmDialog(AppGameBaseActivity.getCurrentActivity(),getResources().getString(R.string.appgame_waiting_loading),
                                getResources().getString(R.string.appgame_button_left),getResources().getString(R.string.appgame_button_right), new IDialogListener() {
                                    @Override
                                    public void onClose()
                                    {
                                        AppGameBaseActivity.getCurrentActivity().finish();
                                    }
                                });
                }
            });
    }

    @Override
    public void onDestroy() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onDestroy();
        }
        super.onDestroy();
    }

    protected abstract void initView(View view);

    protected abstract void initData();

    public boolean onBackPressed() {
        mWebview.runScript("utils.back()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Timber.d("navigation back: %s", value);
            }
        });
        return true;
    }
}
