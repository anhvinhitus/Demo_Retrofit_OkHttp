package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.LinkCardProdureFragment;

public class LinkCardProcedureActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return LinkCardProdureFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
