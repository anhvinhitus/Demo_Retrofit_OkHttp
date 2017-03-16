package vn.com.vng.zalopay.searchcategory;

import android.os.Bundle;
import android.support.v7.widget.SearchView;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.SearchViewFormatter;

/**
 * Created by khattn on 3/10/17.
 * Search Category Activity
 */

public class SearchCategoryActivity extends BaseToolBarActivity {

    @BindView(R.id.searchview)
    SearchView mSearchView;

    @Override
    public BaseFragment getFragmentToHost() {
        return SearchCategoryFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_common_searchbox;
    }
}
