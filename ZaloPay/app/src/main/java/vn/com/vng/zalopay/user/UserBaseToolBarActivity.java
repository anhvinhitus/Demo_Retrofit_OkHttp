package vn.com.vng.zalopay.user;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import vn.com.vng.zalopay.R;

/**
 * Created by hieuvm on 4/18/17.
 * *
 */

public abstract class UserBaseToolBarActivity extends UserBaseActivity {

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     /*   if (!isUserSessionStarted()) {
            return;
        }*/

        setSupportActionBar(mToolbar); // onCreateOptionsMenu sẽ được gọi.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected int getResLayoutId() {
        return R.layout.activity_common_actionbar;
    }

    @Override
    public void setTitle(CharSequence title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleId);
        }
    }

    public void setSubTitle(String subTitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subTitle);
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

}
