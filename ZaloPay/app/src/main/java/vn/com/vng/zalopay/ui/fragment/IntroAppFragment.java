package vn.com.vng.zalopay.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.adapter.IntroAppPagerAdapter;
import vn.com.vng.zalopay.utils.IntroAppUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link IntroAppFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntroAppFragment extends BaseFragment {

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
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    }

    @OnClick(R.id.tvClose)
    public void onClickClose() {
        navigator.startLoginActivity(getContext());
        getActivity().finish();
    }

    private IntroAppPagerAdapter mPagerAdapter;

    public IntroAppFragment() {
        // Required empty public constructor
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment IntroAppFragment.
     */
    // TODO: Rename and change types and number of parameters
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated..............");
        mPagerAdapter = new IntroAppPagerAdapter(getChildFragmentManager(), getIntroResourceIds());
        mViewPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
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

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        IntroAppUtils.setShowedIntro(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
