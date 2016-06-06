package vn.com.vng.zalopay.account.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.account.ui.view.IProfileInfoView;
import vn.com.vng.zalopay.data.repository.datasource.UserConfigFactory;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.utils.ToastUtil;

public class ProfileInfoActivity extends BaseActivity implements IProfileInfoView {

    @Inject
    Navigator navigator;

    @Inject
    UserConfigFactory userConfigFactory;

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

    @BindView(R.id.imgAdsBanner)
    ImageView imgAdsBanner;

//    @OnClick(R.id.layoutUser)
//    public void onClickLayoutUser(View view) {
//        navigator.startPreProfileActivity(this);
//    }

    @OnClick(R.id.layoutSigOutAndDelDB)
    public void onClickSigoutAndDelDB(View view) {
        sigoutAndCleanData();
    }

    @OnClick(R.id.layoutTransactionHistory)
    public void onClickTransactionHistory(View view) {
        navigator.startMiniAppActivity(this, "TransactionLogs");
    }

    @OnClick(R.id.layoutDeposit)
    public void onClickDeposit(View view) {
        navigator.startDepositActivity(this);
    }

    @OnClick(R.id.layoutTransfer)
    public void onClickTransfer(View view) {
        ToastUtil.showToast(this, "Transfer");
    }

    @OnClick(R.id.layoutManagerCard)
    public void onClickManagerCard(View view) {
        ToastUtil.showToast(this, "Manager Card");
    }

    @OnClick(R.id.layoutMyQRCode)
    public void onClickMyQRCode(View view) {
        ToastUtil.showToast(this, "My QRCode");
    }

    private void sigoutAndCleanData() {
        ZaloSDK.Instance.unauthenticate();
        userConfigFactory.clearAllUserDB();
        navigator.startLoginActivity(this, true);
    }

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

    @OnClick(R.id.layoutUserInfo)
    public void onClickUserInfo(View view) {
        navigator.startEditProfileActivity(this);
    }

    public void updateBalance(long balance) {
        tvBalance.setText(CurrencyUtil.formatCurrency(balance, false));
    }

    public void updateBannerView(String bannerUrl) {
        Glide.with(this).load(bannerUrl).placeholder(R.color.separate).centerCrop().into(imgAdsBanner);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_profile_info;
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
