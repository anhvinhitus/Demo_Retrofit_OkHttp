package vn.com.zalopay.game.ui.component.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import timber.log.Timber;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.config.AppGameConfig;
import vn.com.zalopay.game.ui.webview.AppGameWebView;
import vn.com.zalopay.game.ui.webview.AppGameWebViewProcessor;

public class FragmentZingXu extends AppGameFragment
{

    public static FragmentZingXu newInstance()
    {
        FragmentZingXu fragment = new FragmentZingXu();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__zing__xu, container, false);
    }

    @Override
    protected void initView(View rootView)
    {
        mWebview = (AppGameWebView) rootView.findViewById(R.id.webview);

        mWebViewProcessor = new AppGameWebViewProcessor(mWebview);
    }

    @Override
    protected void initData()
    {
        mWebViewProcessor.start(AppGameConfig.ZINGXU_PAGE, getActivity(), new ITimeoutLoadingListener()
        {
            @Override
            public void onTimeoutLoading() {
                Timber.e("onProgressTimeout-%s",AppGameConfig.ZINGXU_PAGE);
            }
        });
    }
}
