package vn.com.zalopay.wallet.ui;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import vn.com.zalopay.wallet.R;

/**
 * Created by chucvv on 6/12/17.
 */

public abstract class ToolbarActivity extends BaseActivity {
    protected Toolbar mToolbar;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }
    }

    public void hideDisplayHome() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    public void centerTitle() {
        TextView textView = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        if (textView != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
            params.gravity = Gravity.CENTER;
            textView.setLayoutParams(params);
        }
    }

    public void setToolbarTitle(CharSequence title) {
        TextView textView = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        if (textView != null) {
            textView.setText(title);
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
