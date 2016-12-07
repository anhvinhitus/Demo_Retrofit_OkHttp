package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.zalopay.ui.widget.viewpager.NonSwipeableViewPager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnPageChange;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.adapter.ChangePinPagerAdapter;
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.account.ui.view.IChangePinContainer;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;

/**
 * Created by AnhHieu on 8/25/16.
 * *
 */
public class ChangePinContainerFragment extends BaseFragment implements IChangePinContainer {

    public static ChangePinContainerFragment newInstance() {

        Bundle args = new Bundle();

        ChangePinContainerFragment fragment = new ChangePinContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_change_pin_container;
    }

    @BindView(R.id.viewPager)
    NonSwipeableViewPager mPager;

    @Inject
    IChangePinPresenter presenter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        mPager.setAdapter(new ChangePinPagerAdapter(getFragmentManager()));
    }

    @OnPageChange(value = R.id.viewPager, callback = OnPageChange.Callback.PAGE_SELECTED)
    public void onPageSelected(int position) {
        if (position == 1) {
            checkAndRequestReadSMSPermission();
        }
    }

    @Override
    public void nextPage() {
        if (mPager != null) {
            int current = mPager.getCurrentItem();
            if (current == 0) {
                mPager.setCurrentItem(current + 1, false);
            } else {
                getActivity().finish();
            }
        }
    }

    @Override
    public void onVerifySuccess() {
        showSuccessDialog(getString(R.string.reset_pin_success), new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {

            }

            @Override
            public void onOKevent() {
                nextPage();
            }
        });
    }

    @Override
    public void onDestroyView() {

        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        presenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        presenter.resume();
        super.onResume();
    }

    @Override
    public void onChangePinOverLimit() {
        getActivity().finish();
    }

    @Override
    public boolean onBackPressed() {
        if (mPager.getCurrentItem() > 0) {
            ZPAnalytics.trackEvent(ZPEvents.OTP_CHANGEPASSWORD_INPUTNONE);
        }

        return super.onBackPressed();
    }
}
