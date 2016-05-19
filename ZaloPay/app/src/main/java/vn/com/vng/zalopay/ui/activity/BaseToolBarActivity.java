package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import vn.com.vng.zalopay.R;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseToolBarActivity extends BaseActivity {

    @Nullable
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected int getResLayoutId() {
        return R.layout.activity_common_actionbar;
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        getSupportActionBar().setTitle(titleId);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
