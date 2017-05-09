package vn.com.vng.zalopay.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.IntroAppFragment;

public class IntroAppActivity extends BaseToolBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        boolean startup = intent.getBooleanExtra("startup", true);
        String title = intent.getStringExtra("title");

        if (getSupportActionBar() == null) {
            return;
        }

        if (startup) {
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
            setTitle(title);
        }
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return IntroAppFragment.newInstance();
    }
}
