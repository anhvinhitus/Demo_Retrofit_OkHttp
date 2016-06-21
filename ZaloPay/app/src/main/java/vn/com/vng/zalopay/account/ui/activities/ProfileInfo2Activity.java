package vn.com.vng.zalopay.account.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.EditProfileFragment;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.account.ui.view.IProfileInfoView;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.CurrencyUtil;

public class ProfileInfo2Activity extends BaseActivity implements IProfileInfoView {

    @Inject
    Navigator navigator;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;

    @Inject
    ProfileInfoPresenter presenter;

    @BindView(R.id.layoutUser)
    View layoutUser;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.tv_name)
    TextView tvName;

    @BindView(R.id.tv_balance)
    TextView tvBalance;

    private EditProfileFragment mEditProfileFragment;
//    @OnClick(R.id.layoutUser)
//    public void onClickLayoutUser(View view) {
//        navigator.startPreProfileActivity(this);
//    }

    public void updateUserInfo(User user) {
        if (user == null) {
            return;
        }
        tvName.setText(user.dname);
        Glide.with(this).load(user.avatar)
                .placeholder(R.color.silver)
                .centerCrop()
                .into(imgAvatar);
    }

    @Override
    public void updateBannerView(String bannerUrl) {

    }

    @BindView(R.id.layoutChangePin)
    View layoutChangePin;

    @OnClick(R.id.layoutChangePin)
    public void onClickChangePin(View view) {
        navigator.startPreProfileActivity(this, null);
    }

    public void updateBalance(long balance) {
        tvBalance.setText(CurrencyUtil.formatCurrency(balance, false));
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_profile_info2;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserComponent().inject(this);
        initView();
        initFragment(savedInstanceState);
    }

    @Override
    public void showHideChangePinView(boolean isShow) {
        if (isShow) {
            layoutChangePin.setVisibility(View.VISIBLE);
        } else {
            layoutChangePin.setVisibility(View.GONE);
        }
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mEditProfileFragment = EditProfileFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, mEditProfileFragment, mEditProfileFragment.TAG);
            ft.commit();
        } else {
            mEditProfileFragment = (EditProfileFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }
        mEditProfileFragment.showHideProfileInfo(false);
    }

    private void initView() {
        presenter.setView(this);
        mToolbar.setTitle("");
        if (getActivity() != null && getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).setSupportActionBar(mToolbar);
            ((BaseActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

//        final Toolbar tool = (Toolbar)findViewById(R.id.toolbar);
//        CollapsingToolbarLayout c = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
//        AppBarLayout appbar = (AppBarLayout)findViewById(R.id.app_bar_layout);
//        tool.setTitle("");
//        setSupportActionBar(tool);
        mCollapsingToolbarLayout.setTitleEnabled(false);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isVisible = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Timber.tag(TAG).d("onOffsetChanged.........verticalOffset:" + verticalOffset);
                float alpha = 1 + ((float)verticalOffset/160);
                Timber.tag(TAG).d("onOffsetChanged.........alpha:" + alpha);
                layoutUser.setAlpha(alpha);
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    mToolbar.setTitle(getString(R.string.personal));
                    isVisible = true;
                } else if (isVisible) {
                    mToolbar.setTitle("");
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
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        super.showToast(message);
    }

    @Override
    public Context getContext() {
        return this;
    }
}

