package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.ProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IProfileView;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.PhoneUtil;


public class EditProfileFragment extends BaseFragment implements IProfileView {


    public static EditProfileFragment newInstance() {

        Bundle args = new Bundle();

        EditProfileFragment fragment = new EditProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    ProfilePresenter mPresenter;

    @BindView(R.id.tvBirthday)
    TextView tvBirthday;

    @BindView(R.id.tvCMND)
    TextView tvCMND;

    @BindView(R.id.tvGender)
    TextView tvGender;

    @BindView(R.id.tvPhone)
    TextView tvPhone;

    @BindView(R.id.tvEmail)
    TextView tvEmail;

    @BindView(R.id.layoutCmnd)
    View mLayoutCmnd;

    @BindView(R.id.layoutEmail)
    View mLayoutEmail;


    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_profile_layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.setView(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.getProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onDestroyView() {
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void updateUserInfo(User user) {

        this.setBirthDay(user.birthDate);
        this.setPhone(user.phonenumber);
        this.setCMND(user.identityNumber);
        this.setEmail(user.email);
        this.setGender(user.getGender());

        if (user.profilelevel < 3) {
            mLayoutCmnd.setClickable(true);
            mLayoutEmail.setClickable(true);
        } else {
            mLayoutCmnd.setClickable(false);
            mLayoutEmail.setClickable(false);
        }
/*
        String zaloPayId = String.valueOf(user.uid);
        String zaloId = String.valueOf(user.zaloId);*/
    }

    private void setBirthDay(long time) {

        Timber.d("setBirthDay: time %s", time);
        Date date = new Date(time * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String birthday = simpleDateFormat.format(date);
        tvBirthday.setText(birthday);
    }

    private void setPhone(long phone) {
        String strPhoneNumber = PhoneUtil.toString(phone);
        if (!TextUtils.isEmpty(strPhoneNumber)) {
            tvPhone.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            tvPhone.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0);
        }
        tvPhone.setText(strPhoneNumber);
    }

    private void setCMND(String cmnd) {
        if (!TextUtils.isEmpty(cmnd)) {
            tvCMND.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            tvCMND.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0);
        }

        tvCMND.setText(cmnd);
    }

    private void setEmail(String email) {
        if (!TextUtils.isEmpty(email)) {
            tvEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            tvEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0);
        }

        tvEmail.setText(email);
    }

    private void setGender(String gender) {
        tvGender.setText(gender);
    }

    @Override
    public void showLoading() {
        super.showProgressDialog();
    }

    @Override
    public void hideLoading() {
        super.hideProgressDialog();
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

    @OnClick(R.id.layoutPhone)
    public void onClickPhone(View v) {
        if (tvPhone.length() == 0) {
            navigator.startUpdateProfileLevel2Activity(getContext(), false);
        }
    }

    @OnClick(R.id.layoutCmnd)
    public void onClickCmnd(View v) {
        if (tvCMND.length() == 0) {
            navigator.startUpdateProfile3Activity(getContext());
        }
    }

    @OnClick(R.id.layoutEmail)
    public void onClickEmail(View v) {
        if (tvEmail.length() == 0) {
            navigator.startUpdateProfile3Activity(getContext());
        }
    }
}
