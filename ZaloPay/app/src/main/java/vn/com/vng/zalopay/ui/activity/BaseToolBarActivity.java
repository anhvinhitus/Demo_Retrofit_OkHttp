package vn.com.vng.zalopay.ui.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.TypefaceHelper;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseToolBarActivity extends BaseActivity {

    @Nullable
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Nullable
    @BindView(R.id.toolbar_title)
    protected TextView mToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initToolbarTitle();
    }

    private void initToolbarTitle() {
        getSupportActionBar().setTitle("");
        if (mToolbarTitle == null) {
            return;
        }
        Typeface myCustomFont = TypefaceHelper.get(this, "fonts/Roboto-Medium.ttf");
        if (myCustomFont != null) {
            mToolbarTitle.setTypeface(myCustomFont);
        }
        mToolbarTitle.setText(this.getTitle());
    }

    protected int getResLayoutId() {
        return R.layout.activity_common_actionbar;
    }

    @Override
    public void setTitle(CharSequence title) {
       //getSupportActionBar().setTitle(title);
        if (mToolbarTitle == null) {
            return;
        }
        mToolbarTitle.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        //getSupportActionBar().setTitle(titleId);
        if (mToolbarTitle == null) {
            return;
        }
        mToolbarTitle.setText(getString(titleId));
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }


}
