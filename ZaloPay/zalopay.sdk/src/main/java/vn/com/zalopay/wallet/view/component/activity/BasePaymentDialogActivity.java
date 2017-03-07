package vn.com.zalopay.wallet.view.component.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.utils.ZPWUtils;

public abstract class BasePaymentDialogActivity extends Activity {
    protected abstract void initData();

    protected abstract void initViews();

    protected abstract void getArguments();

    protected abstract int getLayout();

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.closeslidein, R.anim.closeslideout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //BaseEffects animator = Effectstype.Shake.getAnimator();
        //animator.start(findViewById(R.id.container));
        initData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setFinishOnTouchOutside(false);

        setContentView(getLayout());

        float percentWitdh = getResources().getInteger(R.integer.dialog_percent_ondefault);

        if (ZPWUtils.isTablet(getApplicationContext())) {
            percentWitdh = getResources().getInteger(R.integer.dialog_percent_ontablet);
        }

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * percentWitdh / 100);
        int screenHeigh = (int) (metrics.heightPixels * 0.70);
        getWindow().setLayout(screenWidth, screenHeigh);

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        getArguments();
        initViews();
    }
}
