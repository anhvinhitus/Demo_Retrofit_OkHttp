package vn.com.zalopay.game.ui.component.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
    protected int getResLayoutId() {
        return R.layout.fragment__result;
    }

    @Override
    protected String getWebViewUrl() {
        Timber.d("getWebViewUrl start appTransId: [%s]", AppGameGlobal.getAppGamePayInfo().getApptransid());
        final String url = String.format(AppGameConfig.PAY_RESULT_PAGE, AppGameGlobal.getAppGamePayInfo().getApptransid(),
                AppGameGlobal.getAppGamePayInfo().getUid(), AppGameGlobal.getAppGamePayInfo().getAccessToken());
        Timber.d("getWebViewUrl url [%s]", url);
        return url;
    }
}
