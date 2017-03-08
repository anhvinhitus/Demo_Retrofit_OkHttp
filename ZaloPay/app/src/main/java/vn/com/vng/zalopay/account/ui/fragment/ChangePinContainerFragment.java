package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.viewpager.NonSwipeableViewPager;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnPageChange;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.adapter.ChangePinPagerAdapter;
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.account.ui.view.IChangePinContainer;
import vn.com.vng.zalopay.event.ReceiveOTPEvent;
import vn.com.vng.zalopay.scanners.ui.FragmentLifecycle;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

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

    ChangePinPagerAdapter mAdapter;

    @Inject
    IChangePinPresenter presenter;

    @Inject
    EventBus mEventBus;

    private int mCurrentPage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);
    }

    @OnPageChange(value = R.id.viewPager, callback = OnPageChange.Callback.PAGE_SELECTED)
    public void onPageSelected(int newPosition) {

        Fragment fragmentToShow = mAdapter.getPage(newPosition);
        if (fragmentToShow instanceof FragmentLifecycle) {
            ((FragmentLifecycle) fragmentToShow).onStartFragment();
        }

        Fragment fragmentToHide = mAdapter.getPage(mCurrentPage);
        if (fragmentToHide instanceof FragmentLifecycle) {
            ((FragmentLifecycle) fragmentToHide).onStopFragment();
        }

        mCurrentPage = newPosition;
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
        mEventBus.removeStickyEvent(ReceiveOTPEvent.class);
        presenter.detachView();
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
    public void initPagerContent(int index) {
        mAdapter = new ChangePinPagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mAdapter);
        if (index >= 0 && index < mPager.getAdapter().getCount()) {
            mPager.setCurrentItem(index);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mPager.getCurrentItem() > 0) {
            ZPAnalytics.trackEvent(ZPEvents.OTP_CHANGEPASSWORD_INPUTNONE);
            mPager.setCurrentItem(0);
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String otp = intent.getStringExtra("otp");
        if (!TextUtils.isEmpty(otp)) {
            mEventBus.postSticky(new ReceiveOTPEvent(otp));
        }
    }
}
