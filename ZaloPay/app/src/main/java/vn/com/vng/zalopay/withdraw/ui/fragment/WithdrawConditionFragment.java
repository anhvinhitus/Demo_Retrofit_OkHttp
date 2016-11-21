package vn.com.vng.zalopay.withdraw.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawConditionPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawConditionView;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link WithdrawConditionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WithdrawConditionFragment extends BaseFragment implements IWithdrawConditionView {

    @Inject
    WithdrawConditionPresenter mPresenter;

    @BindView(R.id.tvCardNote)
    View tvCardNote;

    @BindView(R.id.tvUserNote)
    View tvUserNote;

    @BindView(R.id.chkPhone)
    CheckBox chkPhone;

    @BindView(R.id.chkPin)
    CheckBox chkPin;

    @BindView(R.id.chkVietinBank)
    CheckBox chkVietinBank;

    @BindView(R.id.chkSacomBank)
    CheckBox chkSacomBank;

    @BindView(R.id.tvUpdateProfile)
    View tvUpdateProfile;

    @OnClick(R.id.tvUpdateProfile)
    public void onClickUpdateProfile() {
        navigator.startUpdateProfileLevel2Activity(getActivity());
    }

    @OnClick(R.id.tvSaveCard)
    public void onClickSaveCard() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.Withdraw.GO_TO_WITHDRAW_CONDITION, true);
        navigator.startLinkCardActivity(getActivity(), bundle, null);
    }

    public WithdrawConditionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WithdrawConditionFragment.
     */
    public static WithdrawConditionFragment newInstance() {
        return new WithdrawConditionFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_withdraw_condition;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.setView(this);
    }

    @Override
    public void setProfileValid(boolean isValid) {
        if (isValid) {
            chkPhone.setChecked(true);
            chkPin.setChecked(true);
            tvUserNote.setVisibility(View.GONE);
            tvUpdateProfile.setVisibility(View.GONE);
        } else {
            chkPhone.setChecked(false);
            chkPin.setChecked(false);
            tvUserNote.setVisibility(View.VISIBLE);
            tvUpdateProfile.setVisibility(View.VISIBLE);
        }
    }

    public void hideCardNote() {
        tvCardNote.setVisibility(View.GONE);
    }

    public void setChkVietinBank(boolean isChecked) {
        if (chkVietinBank == null) {
            return;
        }
        chkVietinBank.setChecked(isChecked);
    }

    public void setChkSacomBank(boolean isChecked) {
        if (chkSacomBank == null) {
            return;
        }
        chkSacomBank.setChecked(isChecked);
    }

    @Override
    public void onDestroyView() {
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        CShareData.dispose();
        super.onDestroy();
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
        showToast(message);
    }
}
