package vn.com.vng.zalopay.account.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.IconFont;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.account.ui.view.IProfileInfoView;
import vn.com.vng.zalopay.data.util.ConfigLoader;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

public class ProfileActivity extends UserBaseToolBarActivity implements IProfileInfoView {

    @Inject
    ProfileInfoPresenter presenter;

    @Inject
    User user;

    @Inject
    ZaloSdkApi mZaloSdkApi;

    @BindView(R.id.profile_iv_avatar)
    SimpleDraweeView imgAvatar;

    @BindView(R.id.profile_tv_name)
    TextView tvName;

    @BindView(R.id.profile_tv_zalopay_phone_number)
    TextView tvZaloPayPhoneNumber;

    @BindView(R.id.profile_icfont_right_arrow)
    IconFont icfontRightArrow;

    @BindView(R.id.toolbar_profile_tv_title)
    TextView tvToolbarTitle;

    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.ME_PROFILE, -1, ZPEvents.ME_PROFILE_ZPID_TOUCH_BACK);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    public void updateUserInfo(User user) {
        if (user == null) {
            return;
        }
        tvName.setText(user.displayName);
        imgAvatar.setImageURI(user.avatar);
        setPhoneNumber(user.phonenumber);
    }

//    @OnClick(R.id.tab_personal_tv_setting)
//    public void goToProtectAccount() {
//        navigator.startProtectAccount(this);
//    }


    @Override
    public void setPhoneNumber(long phoneNumber) {
        if (tvZaloPayPhoneNumber != null && phoneNumber > 0) {
            String PhoneNumber = String.valueOf(phoneNumber);
            tvZaloPayPhoneNumber.setText(PhoneNumber);
        }
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onUserComponentSetup(@NonNull UserComponent userComponent) {
        userComponent.inject(this);
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return ProfileFragment.newInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isUserSessionStarted()) {
            return;
        }

        initView();
    }

    private void initView() {
        presenter.attachView(this);
        mZaloSdkApi.getProfile();
        icfontRightArrow.setVisibility(View.GONE);
        tvToolbarTitle.setText(getString(R.string.title_activity_profile));
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

        if (!isUserSessionStarted()) {
            super.onDestroy();
            return;
        }

        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void showError(String message) {
        super.showToast(message);
    }
}

