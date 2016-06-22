package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import vn.com.vng.zalopay.R;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseToolBarActivity extends BaseActivity {

    @Nullable
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Nullable
    @BindView(R.id.title_toolbar)
    TextView mTitleToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(mToolbar);

        customToolbar();
    }

    private void customToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getToolbar().getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    protected int getResLayoutId() {
        return R.layout.activity_common_actionbar;
    }

    @Override
    public void setTitle(CharSequence title) {
        if (mTitleToolbar != null) {
            mTitleToolbar.setText(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        if (mTitleToolbar != null) {
            mTitleToolbar.setText(titleId);
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }


}
