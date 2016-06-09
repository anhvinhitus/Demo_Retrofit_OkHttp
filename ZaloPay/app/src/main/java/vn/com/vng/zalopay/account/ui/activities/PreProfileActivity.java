package vn.com.vng.zalopay.account.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.adapter.ProfileSlidePagerAdapter;
import vn.com.vng.zalopay.account.ui.fragment.AbsProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.OtpProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PinProfileFragment;
import vn.com.vng.zalopay.account.ui.presenter.PreProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IPreProfileView;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.mdl.error.PaymentError;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.vng.uicomponent.widget.viewpager.NonSwipeableViewPager;

public class PreProfileActivity extends BaseActivity implements IPreProfileView,
        PinProfileFragment.OnPinProfileFragmentListener,
        OtpProfileFragment.OnOTPFragmentListener {

    private int profileType = 0;
    private ProfileSlidePagerAdapter adapter;
    private String walletTransId = null;
    private PaymentWrapper paymentWrapper;

    @Inject
    Navigator navigator;

    @Inject
    UserConfig userConfig;

    @Inject
    PreProfilePresenter presenter;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.tvSex)
    TextView tvSex;

    @BindView(R.id.tvBirthday)
    TextView tvBirthday;

    @BindView(R.id.tv_name)
    TextView tvName;

    @BindView(R.id.tvTermsOfUser)
    TextView tvTermsOfUser;

    @BindView(R.id.viewPager)
    NonSwipeableViewPager viewPager;

    @OnClick(R.id.btnContinue)
    public void onClickContinue(View view) {
        if (adapter == null) {
            return;
        }
        AbsProfileFragment fragment = (AbsProfileFragment)adapter.getItem(viewPager.getCurrentItem());
        fragment.onClickContinue();
    }

    @Override
    protected void setupActivityComponent() {
        getUserComponent().inject(this);
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.setView(this);
        initData();
        initContent();
        initPaymentWrapper();
    }

    private void initPaymentWrapper() {
        paymentWrapper = new PaymentWrapper(null, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return PreProfileActivity.this;
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                showToast(param);
            }

            @Override
            public void onResponseError(int status) {
                if (status == PaymentError.ERR_CODE_INTERNET) {
                    showToast("Vui lòng kiểm tra kết nối mạng và thử lại.");
                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            }

            @Override
            public void onResponseTokenInvalid() {
            }

            @Override
            public void onResponseCancel() {

            }
        });
    }

    private void initData() {
        Bundle bundle = this.getIntent().getExtras();

        if (bundle == null) {
            return;
        }
        walletTransId = bundle.getString(vn.com.vng.zalopay.domain.Constants.WALLETTRANSID);
    }

    private void initContent() {
        adapter = new ProfileSlidePagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        if (profileType == Constants.PRE_PROFILE_TYPE) {
            viewPager.setCurrentItem(0);
        } else if (profileType == Constants.PIN_PROFILE_TYPE) {
            viewPager.setCurrentItem(1);
        }
    }

    public void nextPager() {
        if (viewPager == null)
            return;
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }
    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    public void updateUserInfo(User user) {
        if (user == null) {
            return;
        }
        Date date = new Date(user.birthDate*1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        tvBirthday.setText(simpleDateFormat.format(date));
        tvName.setText(user.dname);
        tvSex.setText(user.getGender());
        Glide.with(this).load(user.avatar)
                .placeholder(R.color.silver)
                .centerCrop()
                .into(imgAvatar);
        tvTermsOfUser.setClickable(true);
        tvTermsOfUser.setMovementMethod (LinkMovementMethod.getInstance());
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
    public void onUpdatePinSuccess() {
        nextPager();
    }

    @Override
    public void onUpdatePinFail() {

    }

    @Override
    public void onConfirmOTPSucess() {
        showToast("Cập nhật thông tin thành công.");
        if (userConfig == null || userConfig.getCurrentUser() == null) {
            return;
        }
        if (!TextUtils.isEmpty(walletTransId)) {
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
            paymentInfo.zaloUserID = userConfig.getCurrentUser().uid;
            paymentInfo.zaloPayAccessToken = userConfig.getCurrentUser().accesstoken;
            paymentInfo.walletTransID = walletTransId;

            paymentWrapper.saveCardMap(paymentInfo, null);
        } else if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().finish();
        }
    }

    @Override
    public void onConfirmOTPFail() {

    }
}
