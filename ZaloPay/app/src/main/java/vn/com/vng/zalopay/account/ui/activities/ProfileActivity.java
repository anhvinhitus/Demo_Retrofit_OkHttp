package vn.com.vng.zalopay.account.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

public class ProfileActivity extends UserBaseToolBarActivity implements IProfileInfoView, AppBarLayout.OnOffsetChangedListener {

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;

    @Inject
    ProfileInfoPresenter presenter;

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
    ZaloSdkApi mZaloSdkApi;

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
        imgAvatar.setImageURI(user.avatar);
        setZaloPayName(user.zalopayname);
    }

    @Override
    public void setZaloPayName(String zaloPayName) {
        if (TextUtils.isEmpty(zaloPayName)) {
            tvZaloPayName.setText(getString(R.string.zalopay_name_not_update));
        } else {
            tvZaloPayName.setText(String.format(getString(R.string.account_format), zaloPayName));
        }
    }

    @Override
    protected void onUserComponentSetup(@NonNull UserComponent userComponent) {
        userComponent.inject(this);
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

        if (isUserSessionStarted()) {
            presenter.attachView(this);
            initView();
        }
    }

    private void initView() {
        mZaloSdkApi.getProfile();
        getToolbar().setTitleTextColor(Color.TRANSPARENT);

        mCollapsingToolbarLayout.setTitleEnabled(false);
        mAppBarLayout.addOnOffsetChangedListener(this);
        navigator.showSuggestionDialog(this);
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

    boolean isVisible = true;
    int scrollRange = -1;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (isFinishing()) {
            return;
        }

        float alpha = 1 + ((float) verticalOffset / mAppBarLayout.getTotalScrollRange());
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


    @Override
    public void onDestroy() {
        if (isUserSessionStarted()) {
            mAppBarLayout.removeOnOffsetChangedListener(this);
            presenter.detachView();
            presenter.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void showError(String message) {
        super.showToast(message);
    }
}

