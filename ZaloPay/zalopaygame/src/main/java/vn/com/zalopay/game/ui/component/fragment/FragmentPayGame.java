package vn.com.zalopay.game.ui.component.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import timber.log.Timber;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.IDialogListener;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.config.AppGameConfig;
import vn.com.zalopay.game.ui.component.activity.AppGameBaseActivity;
import vn.com.zalopay.game.ui.webview.AppGameWebView;
import vn.com.zalopay.game.ui.webview.AppGameWebViewProcessor;

public class FragmentPayGame extends AppGameFragment {

    public static FragmentPayGame newInstance() {
        FragmentPayGame fragment = new FragmentPayGame();
        return fragment;
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.webapp_fragment_webview;
    }

    @Override
    protected String getWebViewUrl() {
        AppGamePayInfo payInfo = AppGameGlobal.getAppGamePayInfo();
        if (payInfo == null) {
            return "";
        }

        final String url = String.format(
                AppGameConfig.PAYGAME_PAGE,
                payInfo.getUid(),
                payInfo.getAccessToken(),
                payInfo.getAppId());
        Timber.d("getWebViewUrl url [%s]", url);
        return url;
    }

}
