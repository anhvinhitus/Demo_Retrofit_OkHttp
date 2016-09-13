package vn.com.vng.zalopay.game.ui.fragment;

import timber.log.Timber;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.config.AppGameConfig;

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
