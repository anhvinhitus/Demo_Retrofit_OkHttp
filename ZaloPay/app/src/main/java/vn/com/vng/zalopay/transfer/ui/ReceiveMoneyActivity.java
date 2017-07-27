package vn.com.vng.zalopay.transfer.ui;

import android.os.Bundle;
import android.view.WindowManager;

import timber.log.Timber;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPScreens;

/**
 * Created by AnhHieu on 8/25/16.
 * *
 */
public class ReceiveMoneyActivity extends UserBaseToolBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeScreenBrightness(1);
    }


    private void changeScreenBrightness(int screenBrightness) {
        try {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = screenBrightness;
            getWindow().setAttributes(lp);
        } catch (Exception e) {
            Timber.d(e, "change screen brightness");
        }
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return ReceiveMoneyFragment.newInstance();
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.RECEIVEMONEY;
    }
}
