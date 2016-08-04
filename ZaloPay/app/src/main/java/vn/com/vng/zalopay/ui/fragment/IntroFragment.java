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
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.adapter.IntroPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link IntroFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntroFragment extends BaseFragment {

    @BindView(R.id.tvCancel)
    View tvCancel;

    @BindView(R.id.tvContinue)
    View tvContinue;

    @BindView(R.id.tvClose)
    View tvClose;

    @BindView(R.id.indicator)
    SmartTabLayout mIndicator;

    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    @OnClick(R.id.tvCancel)
    public void onClickCancel() {
        getActivity().finish();
    }

    @OnClick(R.id.tvContinue)
    public void onClickContinue() {
        if (mViewPager.getCurrentItem() >= mPagerAdapter.getCount()) {
            getActivity().finish();
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
        }
    }

    @OnClick(R.id.tvClose)
    public void onClickClose() {
        getActivity().finish();
    }

    private IntroPagerAdapter mPagerAdapter;

    public IntroFragment() {
        // Required empty public constructor
    }

    private List<Integer> getIntroResourceIds() {
        List<Integer> introResourceIds = new ArrayList<>();
        introResourceIds.add(R.layout.fragment_intro_step1);
        introResourceIds.add(R.layout.fragment_intro_step2);
        introResourceIds.add(R.layout.fragment_intro_step3);
        return introResourceIds;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment IntroFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IntroFragment newInstance() {
        return new IntroFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_intro;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mPagerAdapter = new IntroPagerAdapter(getContext(), getIntroResourceIds());
        mPagerAdapter = new IntroPagerAdapter(getChildFragmentManager(), getIntroResourceIds());
        mViewPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mViewPager);

        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    tvCancel.setVisibility(View.GONE);
                    tvContinue.setVisibility(View.GONE);
                    tvClose.setVisibility(View.VISIBLE);
                } else {
                    tvCancel.setVisibility(View.VISIBLE);
                    tvContinue.setVisibility(View.VISIBLE);
                    tvClose.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
