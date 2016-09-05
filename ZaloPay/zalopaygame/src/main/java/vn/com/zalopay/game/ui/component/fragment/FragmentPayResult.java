package vn.com.zalopay.game.ui.component.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import timber.log.Timber;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.IDialogListener;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.config.AppGameConfig;
import vn.com.zalopay.game.ui.component.activity.AppGameBaseActivity;
import vn.com.zalopay.game.ui.webview.AppGameWebView;
import vn.com.zalopay.game.ui.webview.AppGameWebViewProcessor;

public class FragmentPayResult extends AppGameFragment {

    public static FragmentPayResult newInstance() {
        FragmentPayResult fragment = new FragmentPayResult();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment__result, container, false);
    }

    @Override
    protected void initView(View rootView) {
        mWebview = (AppGameWebView) rootView.findViewById(R.id.webview);

        mWebViewProcessor = new AppGameWebViewProcessor(mWebview);
    }

    @Override
    protected void initData() {
        Timber.d("initData start appTransId: [%s]", AppGameGlobal.getAppGamePayInfo().getApptransid());
        final String urlPage = String.format(AppGameConfig.PAY_RESULT_PAGE, AppGameGlobal.getAppGamePayInfo().getApptransid(),
                AppGameGlobal.getAppGamePayInfo().getUid(), AppGameGlobal.getAppGamePayInfo().getAccessToken());
        Timber.d("initData url [%s]", urlPage);
        mWebViewProcessor.start(urlPage, getActivity(), new ITimeoutLoadingListener() {
            @Override
            public void onTimeoutLoading() {
                Timber.e("onProgressTimeout-%s", urlPage);
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
