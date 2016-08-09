package vn.com.vng.zalopay.account.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.adapter.ProfileSlidePagerAdapter;
import vn.com.vng.zalopay.account.ui.fragment.AbsProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.OtpProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PinProfileFragment;
import vn.com.vng.zalopay.account.ui.presenter.PreProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IPreProfileView;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.zalopay.wallet.listener.ZPWSaveMapCardListener;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.viewpager.NonSwipeableViewPager;

public class UpdateProfileLevel2Activity extends BaseActivity implements IPreProfileView,
        PinProfileFragment.OnPinProfileFragmentListener,
        OtpProfileFragment.OnOTPFragmentListener {

    private int profileType = 0;
    private ProfileSlidePagerAdapter adapter;
    private String walletTransId = null;
    private PaymentWrapper paymentWrapper;
    private SweetAlertDialog mProgressDialog;
    private String mCurrentPhone = null;

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

    @BindView(R.id.tvTermsOfUser1)
    TextView tvTermsOfUser1;
    @BindView(R.id.tvTermsOfUser2)
    TextView tvTermsOfUser2;
    @BindView(R.id.tvTermsOfUser3)
    TextView tvTermsOfUser3;

    @BindView(R.id.viewPager)
    NonSwipeableViewPager viewPager;

    @OnClick(R.id.btnContinue)
    public void onClickContinue(View view) {
        if (adapter == null) {
            return;
        }
        AbsProfileFragment fragment = (AbsProfileFragment) adapter.getItem(viewPager.getCurrentItem());
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
        final WeakReference<Activity> weakReference = new WeakReference<Activity>(this);
        paymentWrapper = new PaymentWrapper(null, null, null, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return weakReference.get();
            }
        }, null);
    }

    private void initData() {
        Bundle bundle = this.getIntent().getExtras();

        if (bundle == null) {
            return;
        }
        walletTransId = bundle.getString(vn.com.vng.zalopay.domain.Constants.WALLETTRANSID);
        Timber.d("initData, walletTransId %s", walletTransId);
    }

    private void showHideTermOfUser(boolean isShow) {
        if (isShow) {
            tvTermsOfUser1.setVisibility(View.VISIBLE);
            tvTermsOfUser2.setVisibility(View.VISIBLE);
            tvTermsOfUser3.setVisibility(View.VISIBLE);
        } else {
            tvTermsOfUser1.setVisibility(View.GONE);
            tvTermsOfUser2.setVisibility(View.GONE);
            tvTermsOfUser3.setVisibility(View.GONE);
        }
    }

    private void initContent() {
        adapter = new ProfileSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        if (profileType == Constants.PRE_PROFILE_TYPE) {
            viewPager.setCurrentItem(0);
        } else if (profileType == Constants.PIN_PROFILE_TYPE) {
            viewPager.setCurrentItem(1);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                showHideTermOfUser(position == 0);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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

    @Override
    protected void onDestroy() {
        presenter.destroy();
        super.onDestroy();
        hideLoading();
        mProgressDialog = null;
    }

    public void updateUserInfo(User user) {
        if (user == null) {
            return;
        }
        Timber.d("updateUserInfo, birthday: %s", user.birthDate);
        Date date = new Date(user.birthDate * 1000);
        tvBirthday.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
        tvName.setText(user.dname);
        tvSex.setText(user.getGender());
        Glide.with(this).load(user.avatar)
                .placeholder(R.color.silver)
                .centerCrop()
                .into(imgAvatar);
        tvTermsOfUser1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigator.startProfileInfoActivity(UpdateProfileLevel2Activity.this);
            }
        });
        tvTermsOfUser2.setClickable(true);
        tvTermsOfUser2.setMovementMethod (LinkMovementMethod.getInstance());
//        tvTermsOfUser3.setClickable(true);
//        tvTermsOfUser3.setMovementMethod (LinkMovementMethod.getInstance());
        tvTermsOfUser3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showToast(UpdateProfileLevel2Activity.this, "Chức năng sẽ sớm được ra mắt.");
            }
        });
    }

    @Override
    public void showLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }


    @Override
    public void hideLoading() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
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
    public void onUpdatePinSuccess(String phone) {
        mCurrentPhone = phone;
        nextPager();
    }

    @Override
    public void onUpdatePinFail() {

    }

    @Override
    public void onConfirmOTPSuccess() {
        Timber.d("onConfirmOTPSucess, walletTransId: %s", walletTransId);
        showToast("Cập nhật thông tin thành công.");
        presenter.saveUserPhone(mCurrentPhone);
        if (!TextUtils.isEmpty(walletTransId)) {
            showLoading();
            paymentWrapper.saveCardMap(walletTransId, new ZPWSaveMapCardListener() {
                @Override
                public void onSuccess() {
                    if (getActivity() != null) {
                        showToast("Lưu thẻ thành công.");
                        getActivity().finish();
                    }

                }

                @Override
                public void onError(String s) {
                    if (getActivity() != null) {
                        showToast("Lưu thẻ thất bại.");
                        getActivity().finish();
                    }
                }
            });
        } else if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().finish();
        }
    }
}
