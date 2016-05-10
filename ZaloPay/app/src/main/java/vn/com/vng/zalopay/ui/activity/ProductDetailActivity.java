package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.ProductDetailFragment;

public class ProductDetailActivity extends BaseToolBarActivity {

    private String orderId;

    @Override
    public BaseFragment getFragmentToHost() {
        return ProductDetailFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityComponent();
        initData();
    }

    private void initData() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        orderId = bundle.getString(Constants.ORDER_INFO);
    }

    protected void setupActivityComponent() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }
}
