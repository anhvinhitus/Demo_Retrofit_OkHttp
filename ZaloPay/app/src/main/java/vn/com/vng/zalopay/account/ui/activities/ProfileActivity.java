package vn.com.vng.zalopay.account.ui.activities;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.account.ui.view.IProfileInfoView;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.CurrencyUtil;

public class ProfileActivity extends BaseToolBarActivity implements IProfileInfoView {

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

    @OnClick(R.id.layoutProfileInfo)
    public void onClickHeaderProfile() {
        if (!TextUtils.isEmpty(getUserComponent().currentUser().zalopayname)) {
            return;
        }
        navigator.startEditAccountActivity(this);
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
        return ProfileFragment.newInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        presenter.setView(this);
        presenter.getZaloProfileInfo();
        setTitle("");

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
                    setTitle(R.string.personal);
                    isVisible = true;
                } else if (isVisible) {
                    setTitle("");
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
        tvBalance.setText(CurrencyUtil.spanFormatCurrency(balance));
    }
}

