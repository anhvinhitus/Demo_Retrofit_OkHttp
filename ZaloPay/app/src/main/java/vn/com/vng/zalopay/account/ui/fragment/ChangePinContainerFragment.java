package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.zalopay.ui.widget.viewpager.NonSwipeableViewPager;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.adapter.ChangePinPagerAdapter;
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.account.ui.view.IChangePinContainer;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

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
    NonSwipeableViewPager viewPager;

    @Inject
    IChangePinPresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        viewPager.setAdapter(new ChangePinPagerAdapter(getFragmentManager()));
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

    @Override
    public void nextPage() {
        if (viewPager != null) {
            int current = viewPager.getCurrentItem();
            if (current == 0) {
                viewPager.setCurrentItem(current + 1, false);
            } else {
                getActivity().finish();
            }
        }
    }

    @Override
    public void onVerifySuccess() {
        showToast(R.string.reset_pin_success);
        nextPage();
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
}
