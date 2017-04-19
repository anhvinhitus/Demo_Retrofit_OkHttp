package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.LinkBankPagerIndex;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

public class LinkBankActivity extends UserBaseToolBarActivity
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

    protected void onUserComponentSetup(@NonNull UserComponent userComponent) {
        userComponent.inject(this);
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
        mSectionsPagerAdapter = new LinkBankPagerAdapter(getSupportFragmentManager(), bundle);
        
        if (isUserSessionStarted()) {
            mPresenter.attachView(LinkBankActivity.this);
            mPresenter.initPageStart(bundle);
        }

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
    protected void onDestroy() {
        if (isUserSessionStarted()) {
            mPresenter.detachView();
            mPresenter.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
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
