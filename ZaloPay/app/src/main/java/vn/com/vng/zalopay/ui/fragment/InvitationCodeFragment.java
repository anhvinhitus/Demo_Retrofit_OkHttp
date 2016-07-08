package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;


import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.presenter.InvitationCodePresenter;
import vn.com.vng.zalopay.ui.view.IInvitationCodeView;

import vn.com.zalopay.wallet.view.custom.pinview.GridPasswordView;
import vn.vng.uicomponent.widget.button.GuardButton;

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
    GridPasswordView mILCodeView;

    @Inject
    InvitationCodePresenter presenter;

    @BindView(R.id.btnContinue)
    GuardButton mButtonContinueView;

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

        final int invitationCodeLength = getResources().getInteger(R.integer.invitation_code_length);

        mButtonContinueView.registerAvoidMultipleRapidClicks();
        mILCodeView.setPasswordVisibility(true);
        mButtonContinueView.setEnabled(false);

        mILCodeView.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String s) {
                Timber.d("onTextChanged: pass %s", s);
                mButtonContinueView.setEnabled(s.length() == invitationCodeLength);
            }

            @Override
            public void onInputFinish(String s) {

            }
        });


    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @OnClick(R.id.btnContinue)
    public void onClickBtnSend(View v) {
        String code = mILCodeView.getPassWord();
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
