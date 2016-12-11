package vn.com.vng.zalopay.account.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.account.ui.view.IProfileInfoView;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.ImageLoader;
import vn.com.vng.zalopay.utils.ZaloHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

public class ProfileActivity extends BaseToolBarActivity implements IProfileInfoView {

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;

    @Inject
    ProfileInfoPresenter presenter;

    @Inject
    UserConfig userConfig;

    @BindView(R.id.layoutUser)
    View layoutUser;

    @BindView(R.id.imgAvatar)
    SimpleDraweeView imgAvatar;

    @BindView(R.id.tv_name)
    TextView tvName;

    @BindView(R.id.tvZaloPayName)
    TextView tvZaloPayName;

    @Inject
    User user;

    @Inject
    ImageLoader mImageLoader;

    @OnClick(R.id.layoutProfileInfo)
    public void onClickHeaderProfile() {
        if (!TextUtils.isEmpty(user.zalopayname)) {
            return;
        }
        if (getUserComponent().currentUser().profilelevel < 2) {
            navigator.startUpdateProfileLevel2Activity(this);
        } else {
            navigator.startEditAccountActivity(this);
            ZPAnalytics.trackEvent(ZPEvents.UPDATEZPN_LAUNCH_FROMHEADER);
        }
    }

    public void updateUserInfo(User user) {
        if (user == null) {
            return;
        }
        tvName.setText(user.displayName);
        mImageLoader.loadImage(imgAvatar, user.avatar, R.drawable.ic_avatar_default,
                R.drawable.ic_avatar_default, ImageLoader.ScaleType.CENTER);
        setZaloPayName(user.zalopayname);
    }

    @Override
    public void setZaloPayName(String zaloPayName) {
        if (TextUtils.isEmpty(zaloPayName)) {
            tvZaloPayName.setText(getString(R.string.zalopay_name_not_update));
        } else {
            tvZaloPayName.setText(zaloPayName);
        }
    }

    @Override
    protected void setupActivityComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_profile;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return ProfileFragment.newInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        presenter.attachView(this);
        ZaloHelper.getZaloProfileInfo(getApplicationContext(), userConfig);
        getToolbar().setTitleTextColor(Color.TRANSPARENT);

        mCollapsingToolbarLayout.setTitleEnabled(false);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isVisible = true;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (ProfileActivity.this.isFinishing()) {
                    return;
                }

                float alpha = 1 + ((float) verticalOffset / mAppBarLayout.getTotalScrollRange());
                //Timber.d("onOffsetChanged verticalOffset %s alpha %s", verticalOffset, alpha);
                layoutUser.setAlpha(alpha);
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    getToolbar().setTitleTextColor(Color.WHITE);
                    isVisible = true;
                } else if (isVisible) {
                    getToolbar().setTitleTextColor(Color.TRANSPARENT);
                    isVisible = false;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void showError(String message) {
        super.showToast(message);
    }

    @Override
    public void setBalance(long balance) {
    }
}

