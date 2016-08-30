package vn.com.zalopay.game.ui.component.fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.config.AppGameConfig;
import vn.com.zalopay.game.ui.webview.AppGameWebView;
import vn.com.zalopay.game.ui.webview.AppGameWebViewProcessor;

public class FragmentPayResult extends AppGameFragment
{

    public static FragmentPayResult newInstance()
    {
        FragmentPayResult fragment = new FragmentPayResult();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment__result, container, false);
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
        final String urlPage = String.format(AppGameConfig.PAY_RESULT_PAGE, AppGameGlobal.getAppGamePayInfo().getApptransid(),
                AppGameGlobal.getAppGamePayInfo().getUid(),AppGameGlobal.getAppGamePayInfo().getAccessToken());

        mWebViewProcessor.start(urlPage, getActivity(), new ITimeoutLoadingListener()
        {
            @Override
            public void onTimeoutLoading()
            {
                AppGameGlobal.getLog().e("loadUrl-"+urlPage,"onProgressTimeout");
            }
        });
    }
}
