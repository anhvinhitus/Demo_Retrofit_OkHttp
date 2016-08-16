package vn.com.vng.zalopay.account.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.account.ui.view.IProfileInfoView;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.CurrencyUtil;

public class ProfileActivity extends BaseActivity implements IProfileInfoView {

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

    @BindView(R.id.tvZaloPayName)
    TextView tvZaloPayName;

    private ProfileFragment mEditProfileFragment;

    public void updateUserInfo(User user) {
        if (user == null) {
            return;
        }
        tvName.setText(user.dname);
        Glide.with(this).load(user.avatar)
                .placeholder(R.color.silver)
                .centerCrop()
                .into(imgAvatar);
        if (mEditProfileFragment != null) {
            mEditProfileFragment.updateUserInfo(user);
        }
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
        return R.layout.activity_profile_info2;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        if (mEditProfileFragment == null) {
            mEditProfileFragment = ProfileFragment.newInstance();
        }
        return mEditProfileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        presenter.setView(this);
        presenter.getZaloProfileInfo();
        mToolbar.setTitle("");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCollapsingToolbarLayout.setTitleEnabled(false);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isVisible = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float alpha = 1 + ((float) verticalOffset / 160);
                Timber.d("onOffsetChanged verticalOffset %s alpha %s", verticalOffset, alpha);
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

    @Override
    public void setBalance(long balance) {
        tvBalance.setText(CurrencyUtil.spanFormatCurrency(balance));
    }
}

