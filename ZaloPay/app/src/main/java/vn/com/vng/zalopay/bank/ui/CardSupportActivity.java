package vn.com.vng.zalopay.bank.ui;

import android.content.Context;
import android.os.Bundle;

import javax.inject.Inject;

import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.DialogHelper;

public class CardSupportActivity extends BaseToolBarActivity implements ICardSupportView {

    @Inject
    CardSupportPresenter mPresenter;

    @OnClick(R.id.btnContinue)
    public void onClickBtnContinue() {
        mPresenter.addLinkCard();
    }

    @Override
    protected void setupActivityComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return BankSupportFragment.newInstance(true);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_card_support;
    }

    @Override
    public void onPreComplete() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void showLoading() {
        DialogHelper.showLoading(this, null);
    }

    @Override
    public void hideLoading() {
        DialogHelper.hideLoading();
    }

    @Override
    public void showError(String message) {
        super.showErrorDialog(message);
    }

    @Override
    public Context getContext() {
        return this;
    }
}
