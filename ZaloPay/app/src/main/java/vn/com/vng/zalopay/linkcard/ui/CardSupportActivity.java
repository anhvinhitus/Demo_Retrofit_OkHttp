package vn.com.vng.zalopay.linkcard.ui;

import android.content.Context;
import android.os.Bundle;

import javax.inject.Inject;

import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

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
        mPresenter.setView(this);
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return CardSupportFragment.newInstance();
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_card_support;
    }

    @Override
    public void onTokenInvalid() {
        getAppComponent().applicationSession().setMessageAtLogin(getString(R.string.exception_token_expired_message));
        getAppComponent().applicationSession().clearUserSession();
    }

    @Override
    public void onPreComplete() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void showWarningView(String error) {
        showWarningDialog(error, getString(R.string.txt_close), null);
    }

    @Override
    public void showLoading() {
        DialogManager.showProcessDialog(this, null);
    }

    @Override
    public void hideLoading() {
        DialogManager.closeProcessDialog();
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        DialogManager.showSweetDialogCustom(getActivity(), message,
                getString(R.string.txt_close), SweetAlertDialog.ERROR_TYPE, null);
    }

    @Override
    public Context getContext() {
        return this;
    }
}
