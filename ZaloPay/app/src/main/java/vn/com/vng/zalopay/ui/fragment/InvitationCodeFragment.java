package vn.com.vng.zalopay.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.event.ReceiveSmsEvent;
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

    @BindView(R.id.lb_invite)
    TextView mTvInviteView;

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
        super.showProgressDialog();
    }

    @Override
    public void hideLoading() {
        super.hideProgressDialog();
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

    @Override
    public void showLabelError() {
        mTvInviteView.setTextColor(Color.RED);
        mTvInviteView.setText(R.string.exception_code_invalid);
    }

    @Override
    public void onResume() {
        super.onResume();

        Timber.d("Resume invitation code fragment");
        try {
            detectInvitationCode();
        } catch (Throwable ex) {
            Timber.w(ex, "Failed to detect invitation code from clipboard");
        }
    }

    private void detectInvitationCode() {
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (!clipboardManager.hasPrimaryClip()) {
            Timber.d("Clipboard is empty");
            return;
        }

        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            Timber.d("Clipboard is empty");
            return;
        }

        ClipData.Item item = clipData.getItemAt(0);
        CharSequence itemText = item.getText();
        if (itemText == null || itemText.length() == 0) {
            Timber.d("Cannot get text from Clipboard");
            return;
        }

        String value = itemText.toString();
        Timber.d("Text on clipboard: %s", value);

        String pattern = "(.*)(\\d{8})(.*)";
        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(value);
        if (m.find()) {
            Timber.d("Found Invitation code: %s", m.group(2));
            mILCodeView.setPassword(m.group(2));
            mButtonContinueView.setEnabled(true);
        } else {
            Timber.d("Could not find any invitation code");
        }
    }
}
