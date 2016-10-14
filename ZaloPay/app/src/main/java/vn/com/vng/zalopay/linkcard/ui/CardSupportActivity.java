package vn.com.vng.zalopay.linkcard.ui;

import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class CardSupportActivity extends BaseToolBarActivity {

    @OnClick(R.id.btnContinue)
    public void onClickBtnContinue() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return CardSupportFragment.newInstance();
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_card_support;
    }
}
