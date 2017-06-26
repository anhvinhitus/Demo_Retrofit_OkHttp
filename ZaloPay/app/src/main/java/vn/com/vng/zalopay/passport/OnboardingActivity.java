package vn.com.vng.zalopay.passport;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
            DrawableCompat.setTint(navigationIcon, ContextCompat.getColor(this, R.color.blue_008fe5));
            navigationIcon.setAlpha(255);
        }

        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, getToolbar());
    }

    public BaseFragment getFragmentToHost() {
        return OnboardingFragment.newInstance();
    }

    public int getResLayoutId() {
        return R.layout.activity_onboarding;
    }
}