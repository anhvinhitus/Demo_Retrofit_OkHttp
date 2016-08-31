package vn.com.zalopay.game.ui.component.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import timber.log.Timber;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.ui.webview.AppGameWebView;
import vn.com.zalopay.game.ui.webview.AppGameWebViewProcessor;

/**
 * Created by admin on 8/28/16.
 */
public abstract class AppGameFragment extends Fragment
{
    protected AppGameWebView mWebview;
    protected AppGameWebViewProcessor mWebViewProcessor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        initView(view);
        initData();

        super.onViewCreated(view, savedInstanceState);
    }

    public boolean canBack()
    {
        boolean canBack = false;

        if(mWebview != null)
        {
            canBack = mWebview.canGoBack();
        }

        return canBack;
    }

    public void goBack()
    {
        if(mWebview != null)
            mWebview.goBack();
    }

    public void loadUrl(final String pUrl)
    {
        if(mWebViewProcessor != null)
            mWebViewProcessor.start(pUrl, getActivity(), new ITimeoutLoadingListener()
            {
                @Override
                public void onTimeoutLoading() {
                    Timber.e("onProgressTimeout-%s",pUrl);
                }
            });
    }

    protected abstract void initView(View view);

    protected abstract void  initData();
}
