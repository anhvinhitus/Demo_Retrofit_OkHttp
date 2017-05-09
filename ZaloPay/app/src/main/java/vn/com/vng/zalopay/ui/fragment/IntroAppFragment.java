package vn.com.vng.zalopay.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnPageChange;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.adapter.IntroAppPagerAdapter;
import vn.com.vng.zalopay.utils.IntroAppUtils;


public class IntroAppFragment extends BaseFragment {

    public static IntroAppFragment newInstance() {
        return new IntroAppFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getAppComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_intro_app;
    }

    @BindView(R.id.tvStart)
    View tvStart;

    @BindView(R.id.imgContinue)
    View imgContinue;

    @BindView(R.id.tvClose)
    View tvClose;

    @BindView(R.id.indicator)
    SmartTabLayout mIndicator;

    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    private IntroAppPagerAdapter mPagerAdapter;

    private boolean startup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPagerAdapter = new IntroAppPagerAdapter(getChildFragmentManager(), getIntroResourceIds());
        Intent intent = getActivity().getIntent();
        startup = intent.getBooleanExtra("startup", true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mViewPager);
        IntroAppUtils.setShowedIntro(true);

        if (!startup) {
            tvStart.setVisibility(View.GONE);
            imgContinue.setVisibility(View.GONE);
            tvClose.setVisibility(View.GONE);
        }
    }

    @OnPageChange(R.id.viewPager)
    public void onPageSelected(int position) {
        if (!startup) {
            return;
        }

        if (position == (mPagerAdapter.getCount() - 1)) {
            tvStart.setVisibility(View.GONE);
            imgContinue.setVisibility(View.GONE);
            tvClose.setVisibility(View.VISIBLE);
        } else {
            tvStart.setVisibility(View.VISIBLE);
            imgContinue.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.tvStart)
    public void onClickStart() {
        navigator.startLoginActivity(getContext());
        getActivity().finish();
    }

    @OnClick(R.id.imgContinue)
    public void onClickContinue() {
        if (mViewPager.getCurrentItem() >= mPagerAdapter.getCount()) {
            getActivity().finish();
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
        }
    }

    @OnClick(R.id.tvClose)
    public void onClickClose() {
        navigator.startLoginActivity(getContext());
        getActivity().finish();
    }

    private List<Integer> getIntroResourceIds() {
        List<Integer> introResourceIds = new ArrayList<>();
        introResourceIds.add(R.layout.fragment_intro_app_step1);
        introResourceIds.add(R.layout.fragment_intro_app_step2);
        introResourceIds.add(R.layout.fragment_intro_app_step3);
        introResourceIds.add(R.layout.fragment_intro_app_step4);
        introResourceIds.add(R.layout.fragment_intro_app_step5);
        return introResourceIds;
    }

}
