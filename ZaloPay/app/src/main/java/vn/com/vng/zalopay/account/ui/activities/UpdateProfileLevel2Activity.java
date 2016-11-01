package vn.com.vng.zalopay.account.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.viewpager.NonSwipeableViewPager;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.adapter.ProfileSlidePagerAdapter;
import vn.com.vng.zalopay.account.ui.fragment.OtpProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PinProfileFragment;
import vn.com.vng.zalopay.account.ui.presenter.PreProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IPreProfileView;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.RefreshPaymentSdkEvent;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.ImageLoader;
import vn.com.zalopay.wallet.listener.ZPWSaveMapCardListener;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

public class UpdateProfileLevel2Activity extends BaseToolBarActivity implements IPreProfileView,
        PinProfileFragment.OnPinProfileFragmentListener,
        OtpProfileFragment.OnOTPFragmentListener {

    private ProfileSlidePagerAdapter adapter;
    private String walletTransId = null;
    private PaymentWrapper paymentWrapper;
    private SweetAlertDialog mProgressDialog;
    private String mCurrentPhone = null;
    private String mCurrentZaloPayName = "";

    @Inject
    PreProfilePresenter presenter;

    @Inject
    ImageLoader mImageLoader;

    @BindView(R.id.headerView)
    View headerView;

    @BindView(R.id.imgAvatar)
    SimpleDraweeView imgAvatar;

    @BindView(R.id.tvSex)
    TextView tvSex;

    @BindView(R.id.tvBirthday)
    TextView tvBirthday;

    @BindView(R.id.tv_name)
    TextView tvName;

    @BindView(R.id.viewPager)
    NonSwipeableViewPager viewPager;

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
        return R.layout.activity_update_profile_level2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.setView(this);
        initData();
        initPaymentWrapper();
        headerView.setVisibility(View.GONE);
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

    @Override
    public void initPagerContent(int pageIndex) {
        adapter = new ProfileSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        if (pageIndex >= 0 && pageIndex < viewPager.getAdapter().getCount()) {
            viewPager.setCurrentItem(pageIndex);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    checkAndRequestReadSMSPermission();
                }
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
        tvName.setText(user.displayName);
        tvSex.setText(user.getGender());
        mImageLoader.loadImage(imgAvatar, user.avatar);
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
    public void onUpdatePinSuccess(String phone, String zaloPayName) {
        mCurrentPhone = phone;
        mCurrentZaloPayName = zaloPayName;
        nextPager();
    }

    @Override
    public void onConfirmOTPSuccess() {
        Timber.d("onConfirmOTPSucess, walletTransId: %s", walletTransId);
        showToast("Cập nhật thông tin thành công.");
        presenter.saveUserPhone(mCurrentPhone);
        presenter.saveZaloPayName(mCurrentZaloPayName);
        //Reload PaymentSDK for load new payment permission
        EventBus.getDefault().post(new RefreshPaymentSdkEvent());
        if (!TextUtils.isEmpty(walletTransId)) {
            showLoading();
            paymentWrapper.saveCardMap(walletTransId, new ZPWSaveMapCardListener() {
                @Override
                public void onSuccess() {
                    if (getActivity() == null) {
                        return;
                    }
                    showToastLonger(getString(R.string.txt_link_card_success));
                    getActivity().finish();
                }

                @Override
                public void onError(String s) {
                    if (getActivity() == null) {
                        return;
                    }
                    if (TextUtils.isEmpty(s)) {
                        showToast(getString(R.string.txt_link_card_fail));
                    } else {
                        showToastLonger(s);
                    }
                    getActivity().finish();
                }
            });
        } else if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager != null && adapter != null) {
            if (viewPager.getCurrentItem() == 0) {
                adapter.getItem(0).onBackPressed();
            } else {
                viewPager.setCurrentItem(0);
                return;
            }
        }
        super.onBackPressed();
    }
}
