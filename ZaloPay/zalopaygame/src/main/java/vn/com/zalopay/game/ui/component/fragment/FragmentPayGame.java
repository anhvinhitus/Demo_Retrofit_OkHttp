package vn.com.zalopay.game.ui.component.fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.IDialogListener;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.config.AppGameConfig;
import vn.com.zalopay.game.ui.component.activity.AppGameBaseActivity;
import vn.com.zalopay.game.ui.webview.AppGameWebView;
import vn.com.zalopay.game.ui.webview.AppGameWebViewProcessor;

public class FragmentPayGame extends AppGameFragment
{

    public static FragmentPayGame newInstance()
    {
        FragmentPayGame fragment = new FragmentPayGame();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.webapp_fragment_webview, container, false);
    }

    @Override
    protected void initView(View rootView)
    {
        mWebview = (AppGameWebView) rootView.findViewById(R.id.webview);

        mWebViewProcessor = new AppGameWebViewProcessor(mWebview);
    }

    @Override
    protected void initData() {
        AppGamePayInfo payInfo = AppGameGlobal.getAppGamePayInfo();
        if (payInfo == null) {
            return;
        }

        String urlPage = String.format(
                AppGameConfig.PAYGAME_PAGE,
                payInfo.getUid(),
                payInfo.getAccessToken(),
                payInfo.getAppId());

        mWebViewProcessor.start(urlPage, getActivity(), new ITimeoutLoadingListener() {
            @Override
            public void onTimeoutLoading() {
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
}
