package vn.com.vng.zalopay.account.ui.activities;

import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.adapter.ChangePinSlidePagerAdapter;
import vn.com.vng.zalopay.account.ui.fragment.AbsProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.ChangePinFragment;
import vn.com.vng.zalopay.account.ui.fragment.OTPRecoveryPinFragment;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.vng.uicomponent.widget.viewpager.NonSwipeableViewPager;

public class ChangePinActivity extends BaseToolBarActivity implements
        ChangePinFragment.OnPinProfileFragmentListener,
        OTPRecoveryPinFragment.OnOTPFragmentListener {

    private int profileType = 0;
    private ChangePinSlidePagerAdapter adapter;

    @Inject
    Navigator navigator;

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
        return R.layout.activity_change_pin;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContent();
    }

    private void initContent() {
        adapter = new ChangePinSlidePagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
    }

    public void nextPager() {
        if (viewPager == null)
            return;
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    @Override
    public void onResume() {
        super.onResume();
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
        showToast("Đôi mã PIN thành công.");
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().finish();
        }
    }

    @Override
    public void onConfirmOTPFail() {

    }
}
