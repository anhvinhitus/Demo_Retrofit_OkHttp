package vn.com.vng.zalopay.webview.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

public class WebViewFromQRScanActivity extends UserBaseToolBarActivity {
    @BindView(R.id.title)
    TextView tvTitle;

    @BindView(R.id.btn_back)
    IconFont btnBack;

    @BindView(R.id.btn_close)
    IconFont btnClose;

    @OnClick(R.id.close_view)
    void onCloseWebAppClicked() {
        finish();
    }

    @OnClick(R.id.back_view)
    void onBackWebAppClicked() {
        onBackPressed();
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_common_actionbar_white;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return WebViewFromQRScanFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        btnBack.setIcon(R.string.general_backandroid);
        btnBack.setIconColor(R.color.colorWebAppPrimaryText);
        btnBack.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);

        btnClose.setIcon(R.string.webapp_ico_delete);
        btnClose.setIconColor(R.color.colorWebAppPrimaryText);
        btnClose.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);

//        getToolbar().setNavigationIcon(
//                new IconFontDrawable(this)
//                        .setIcon(R.string.general_backandroid)
//                        .setResourcesColor(R.color.colorWebAppPrimaryText)
//                        .setDpSize(18)
//        );
    }

    @Override
    public void setTitle(CharSequence title) {
        tvTitle.setText(title);
    }
}
