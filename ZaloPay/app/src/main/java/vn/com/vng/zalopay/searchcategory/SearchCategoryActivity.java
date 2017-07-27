package vn.com.vng.zalopay.searchcategory;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPScreens;

/**
 * Created by khattn on 3/10/17.
 * Search Category Activity
 */

public class SearchCategoryActivity extends UserBaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return SearchCategoryFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_common_searchbox;
    }

    @NonNull
    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.SEARCH;
    }
}
