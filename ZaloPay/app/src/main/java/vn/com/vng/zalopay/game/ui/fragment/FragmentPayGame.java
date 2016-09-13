package vn.com.vng.zalopay.game.ui.fragment;

import timber.log.Timber;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;
import vn.com.zalopay.game.config.AppGameConfig;

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
