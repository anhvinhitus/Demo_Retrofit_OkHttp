package vn.com.vng.zalopay.passport;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.jaeger.library.StatusBarUtil;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;


/**
 * Created by hieuvm on 6/9/17.
 * *
 */
public class OnboardingActivity extends BaseToolBarActivity {

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Drawable navigationIcon = getToolbar().getNavigationIcon();

        if (navigationIcon != null) {
            DrawableCompat.setTint(navigationIcon, Color.BLACK);
            navigationIcon.setAlpha(255);
        }

        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, findViewById(R.id.toolbar));
    }

    public BaseFragment getFragmentToHost() {
        return OnboardingFragment.newInstance();
    }

    public int getResLayoutId() {
        return R.layout.activity_onboarding;
    }
}