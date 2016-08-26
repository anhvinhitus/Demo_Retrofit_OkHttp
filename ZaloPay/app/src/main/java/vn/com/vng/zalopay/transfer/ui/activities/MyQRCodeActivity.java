package vn.com.vng.zalopay.transfer.ui.activities;

import android.os.Bundle;
import android.view.WindowManager;

import timber.log.Timber;
import vn.com.vng.zalopay.transfer.ui.fragment.MyQRCodeFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 8/25/16.
 */
public class MyQRCodeActivity extends BaseToolBarActivity {

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
        return MyQRCodeFragment.newInstance();
    }
}
