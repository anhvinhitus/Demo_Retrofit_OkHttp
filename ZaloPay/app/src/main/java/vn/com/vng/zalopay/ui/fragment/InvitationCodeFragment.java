package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.presenter.InvitationCodePresenter;
import vn.com.vng.zalopay.ui.view.IInvitationCodeView;
import vn.com.vng.zalopay.ui.widget.PassCodeView;

/**
 * Created by AnhHieu on 6/27/16.
 */
public class InvitationCodeFragment extends BaseFragment implements IInvitationCodeView {

    public static InvitationCodeFragment newInstance() {

        Bundle args = new Bundle();

        InvitationCodeFragment fragment = new InvitationCodeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getAppComponent().inject(this);
    }


    @BindView(R.id.passCodeInput)
    PassCodeView mILCodeView;

    @Inject
    InvitationCodePresenter presenter;

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_invitation_code;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        mILCodeView.setButtonHideVisibility(View.GONE);
        mILCodeView.setHintVisibility(View.GONE);
        mILCodeView.setBackgroundEdittext(0);
        mILCodeView.showPasscode();
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @OnClick(R.id.btnContinue)
    public void onClickBtnSend(View v) {
        String code = mILCodeView.getText().trim();
        if (TextUtils.isEmpty(code)) {
            showToast(R.string.invitation_code_empty_error);
        } else {
            presenter.sendCode(code);
        }
    }

    @Override
    public void showLoading() {
        showProgressDialog();
    }

    @Override
    public void hideLoading() {
        hideProgressDialog();
    }

    @Override
    public void gotoMainActivity() {
        navigator.startHomeActivity(getContext(), true);
        getActivity().finish();
    }

    @Override
    public void showError(String m) {
        showToast(m);
    }
}
