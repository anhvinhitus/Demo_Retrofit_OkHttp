package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.edittext.ZPEditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.EditAccountNamePresenter;
import vn.com.vng.zalopay.account.ui.view.IEditAccountNameView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.validate.MinCharactersValidate;
import vn.com.vng.zalopay.ui.widget.validate.SpecialCharactersValidate;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 8/12/16.
 */
public class EditAccountNameFragment extends BaseFragment implements IEditAccountNameView {

    public static EditAccountNameFragment newInstance() {

        Bundle args = new Bundle();

        EditAccountNameFragment fragment = new EditAccountNameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_edit_account_name_layout;
    }

    @BindView(R.id.btnCheck)
    Button mBtnCheckView;

    @BindView(R.id.edtAccountName)
    ZPEditText mAccountNameView;

    @BindView(R.id.ivCheck)
    View mCheckView;

    @Inject
    EditAccountNamePresenter presenter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);
        mBtnCheckView.setEnabled(false);
        mCheckView.setVisibility(View.INVISIBLE);
        mBtnCheckView.setText(R.string.check);

        mAccountNameView.addValidator(new MinCharactersValidate(getString(R.string.exception_account_name_length), 4));
        mAccountNameView.addValidator(new SpecialCharactersValidate(getString(R.string.exception_account_name_special_char)));
        mAccountNameView.setClearTextListener(new ZPEditText.OnClearTextListener() {
            @Override
            public void onClearTextSuccess() {
                ZPAnalytics.trackEvent(ZPEvents.UPDATEZPN_EDIT_DELETE);
            }
        });
    }

    @OnTextChanged(value = R.id.edtAccountName, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTextChangeAccountName(CharSequence s) {
        mCheckView.setVisibility(View.INVISIBLE);
        boolean isValid = mAccountNameView.isValid();
        mBtnCheckView.setEnabled(isValid);
        mBtnCheckView.setText(R.string.check);
    }

    @Override
    public void onDestroyView() {
        presenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
    }


    @OnClick(R.id.btnCheck)
    public void onClickCheck(Button v) {
        if (!mAccountNameView.validate()) {
            return;
        }

        String btnText = v.getText().toString();
        final String accountName = mAccountNameView.getText().toString();
        if (btnText.equals(getString(R.string.check))) {
            ZPAnalytics.trackEvent(ZPEvents.UPDATEZPN_PRESSCHECK);
            presenter.existAccountName(accountName);
        } else {
            confirmUpdateAccount(accountName);
        }
    }

    private void confirmUpdateAccount(final String accountName) {
        super.showConfirmDialog(getString(R.string.confirm_update_account_name),
                getString(R.string.accept),
                getString(R.string.cancel),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onOKevent() {
                        if (presenter != null) {
                            presenter.updateAccountName(accountName);
                        }
                    }

                    @Override
                    public void onCancelEvent() {
                    }
                });
    }

    @Override
    public void showError(String msg) {
        showToast(msg);
    }

    @Override
    public void accountNameValid(boolean isValid) {
        mCheckView.setSelected(isValid);
        mCheckView.setVisibility(View.VISIBLE);
        if (isValid) {
            mBtnCheckView.setText(R.string.register);
            ZPAnalytics.trackEvent(ZPEvents.UPDATEPROFILE2_ZPN_VALID);
        } else {
            mAccountNameView.setError(getContext().getString(R.string.account_existed));
            mBtnCheckView.setText(R.string.check);
            ZPAnalytics.trackEvent(ZPEvents.UPDATEZPN_EDIT_INVALID);
        }
    }

    @Override
    public void editAccountNameSuccess() {
        showSuccessDialog(getString(R.string.update_account_name_success),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {

                    }

                    @Override
                    public void onOKevent() {
                        getActivity().finish();
                    }
                });
    }

    @Override
    public void showLoading() {
        showProgressDialog();
    }

    @Override
    public void hideLoading() {
        hideProgressDialog();
    }
}
