package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.w3c.dom.Text;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.EditAccountNamePresenter;
import vn.com.vng.zalopay.account.ui.view.IEditAccountNameView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.ZPTextInputLayout;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

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

    @BindView(R.id.textInputZaloPayName)
    ZPTextInputLayout mInputAccountNameView;

    @Inject
    EditAccountNamePresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        mBtnCheckView.setEnabled(false);
    }

    @OnTextChanged(R.id.edtZaloPayName)
    public void onTextChangeAccountName(CharSequence s) {

        boolean isValid = false;
        if (!ValidateUtil.isValidLengthZPName(s.toString())) {
            mInputAccountNameView.setError(getString(R.string.exception_account_name_length));
        } else if (s.toString().indexOf(" ") > 0) {
            mInputAccountNameView.setError(getString(R.string.exception_account_name_with_space));
        } else if (!ValidateUtil.isValidZaloPayName(s.toString())) {
            mInputAccountNameView.setError(getString(R.string.exception_account_name_special_char));
        } else {
            mInputAccountNameView.setError("");
            mInputAccountNameView.setStateWithoutIcon(ZPTextInputLayout.ViewState.UNKNOWN);

            if (!TextUtils.isEmpty(s)) {
                isValid = true;
            }
        }

        mBtnCheckView.setEnabled(isValid);
        mInputAccountNameView.setStateWithoutIcon(ZPTextInputLayout.ViewState.UNKNOWN);
        mBtnCheckView.setText(R.string.check);
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
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
    public void onClickCheck(View v) {

        final String accountName = mInputAccountNameView.getText();
        if (TextUtils.isEmpty(accountName)) {
            return;
        }

        if (mInputAccountNameView.isValid()) {
            showDialog(getString(R.string.notification), getString(R.string.confirm_update_account_name),
                    getString(R.string.cancel),
                    getString(R.string.accept), new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            presenter.updateAccountName(accountName);
                            sweetAlertDialog.dismiss();
                        }
                    }, SweetAlertDialog.NORMAL_TYPE);

        } else if (mInputAccountNameView.isUnknown()) {
            presenter.existAccountName(accountName);
        }
    }

    @Override
    public void showError(String msg) {
        showToast(msg);
    }

    @Override
    public void accountNameValid(boolean isValid) {
        if (isValid) {
            mInputAccountNameView.setStateWithIconDefault(ZPTextInputLayout.ViewState.VALID);
            mBtnCheckView.setText(R.string.register);
        } else {
            mInputAccountNameView.setStateWithIconDefault(ZPTextInputLayout.ViewState.INVALID);
            mBtnCheckView.setText(R.string.check);
        }
    }

    @Override
    public void editAccountNameSuccess() {
        showToast(R.string.update_account_name_success);
        getActivity().finish();
    }
}
