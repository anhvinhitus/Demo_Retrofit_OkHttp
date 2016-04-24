package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;


import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import butterknife.Bind;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.TabMainInformation;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.TabView;


/**
 * Created by AnhHieu on 4/21/16.
 */
public class ZPHomeActivity extends BaseToolBarActivity {


    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    private final String TAG = this.getClass().getSimpleName();

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @Bind(R.id.indicator)
    SmartTabLayout viewPagerTab;

    FragmentPagerItemAdapter adapter;

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("savedInstanceState " + savedInstanceState);

        if (getToolbar() != null) {
            getToolbar().setTitle(null);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        FragmentPagerItems pages = new FragmentPagerItems(this);
        for (TabMainInformation i : TabMainInformation.values()) {
            pages.add(FragmentPagerItem.of(getString(i.getTitleResId()), i.getClassFragment()));
        }
        adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), pages);

        viewPager.setAdapter(adapter);


        viewPagerTab.setCustomTabView(new TabMainInformation.SimpleTabProvider());

        viewPagerTab.setViewPager(viewPager);

        viewPagerTab.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                TabView tab = (TabView) viewPagerTab.getTabAt(position);
                if (position == 2) {
                    tab.showNotification();
                }
            }
        });
    }
}
