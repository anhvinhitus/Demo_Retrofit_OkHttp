package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.EditAccountNamePresenter;
import vn.com.vng.zalopay.account.ui.view.IEditAccountNameView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.ZPTextInputLayout;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
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

        if (!mInputAccountNameView.isUnknown()) {
            ZPAnalytics.trackEvent(ZPEvents.UPDATEZPN_EDIT_AFTERCHECK);
        }

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

        ZPAnalytics.trackEvent(ZPEvents.UPDATEZPN_PRESSCHECK);

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
            ZPAnalytics.trackEvent(ZPEvents.UPDATEPROFILE2_ZPN_VALID);
        } else {
            mInputAccountNameView.setStateWithIconDefault(ZPTextInputLayout.ViewState.INVALID);
            mInputAccountNameView.setError(getContext().getString(R.string.account_existed));
            mBtnCheckView.setText(R.string.check);
            ZPAnalytics.trackEvent(ZPEvents.UPDATEZPN_EDIT_INVALID);
        }
    }

    @Override
    public void editAccountNameSuccess() {
        showToast(R.string.update_account_name_success);
        getActivity().finish();
    }
}
