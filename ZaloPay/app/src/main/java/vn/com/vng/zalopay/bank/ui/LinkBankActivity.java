package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.LinkBankPagerIndex;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class LinkBankActivity extends BaseToolBarActivity
        implements ILinkBankView, LinkCardFragment.ILinkCardListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private LinkBankPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Inject
    LinkBankPresenter mPresenter;

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected void setupActivityComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_link_bank;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = null;
        if (getIntent() != null) {
            bundle = getIntent().getExtras();
        }

        mPresenter.attachView(LinkBankActivity.this);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new LinkBankPagerAdapter(getSupportFragmentManager(), bundle);

        mPresenter.initPageStart(bundle);
    }

    @Override
    public boolean initViewPager(int pageIndex) {
        Timber.d("Change current page to index[%s]", pageIndex);
        if (pageIndex < 0 || pageIndex >= mSectionsPagerAdapter.getCount()) {
            return false;
        }

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(pageIndex);
        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount() - 1);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        return true;
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = getSharedPreferences(Constants.PREF_LINK_BANK, MODE_PRIVATE).edit();
        int curPosition = mViewPager.getCurrentItem();
        editor.putInt(Constants.PREF_LINK_BANK_LAST_INDEX, curPosition);
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onDetachedFromWindow() {
        mPresenter.detachView();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {
        super.showErrorDialog(message);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void gotoTabLinkAccount() {
        if (mViewPager == null) {
            return;
        }
        if (mViewPager.getCurrentItem() == LinkBankPagerIndex.LINK_ACCOUNT.getValue()) {
            return;
        }
        mViewPager.setCurrentItem(LinkBankPagerIndex.LINK_ACCOUNT.getValue());
    }
}
