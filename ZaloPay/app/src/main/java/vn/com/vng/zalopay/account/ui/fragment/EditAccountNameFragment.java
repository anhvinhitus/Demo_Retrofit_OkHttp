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
    }

    @OnTextChanged(R.id.edtZaloPayName)
    public void onTextChangeAccountName(CharSequence s) {
        if (!ValidateUtil.isValidLengthZPName(s.toString())) {
            mInputAccountNameView.setError("Tên tài khoản phải từ 4-24 ký tự");
        } else if (!ValidateUtil.isValidZaloPayName(s.toString())) {
            mInputAccountNameView.setError("Tên tài khoản chỉ chứa các ký tự từ A-Z, a-z, 0-9");
        } else {
            mInputAccountNameView.setError("");
            mInputAccountNameView.setStateWithoutIcon(ZPTextInputLayout.ViewState.UNKNOWN);
        }

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

        Timber.d("onClickCheck");

        String accountName = mInputAccountNameView.getText();
        if (TextUtils.isEmpty(accountName)) {
            return;
        }

        if (mInputAccountNameView.isValid()) {
            presenter.updateAccountName(accountName);
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
