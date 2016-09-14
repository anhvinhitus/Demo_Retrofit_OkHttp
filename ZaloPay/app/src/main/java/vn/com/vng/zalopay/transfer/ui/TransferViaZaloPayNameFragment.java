package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.TransferMoneyViaAccountNamePresenter;
import vn.com.vng.zalopay.ui.view.ITransferMoneyView;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
public class TransferViaZaloPayNameFragment extends BaseFragment implements ITransferMoneyView {

    public static TransferViaZaloPayNameFragment newInstance() {

        Bundle args = new Bundle();

        TransferViaZaloPayNameFragment fragment = new TransferViaZaloPayNameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_transfer_via_zalopay_name;
    }

    @BindView(R.id.textInputAccountName)
    TextInputLayout textInputAccountName;

    @Inject
    TransferMoneyViaAccountNamePresenter presenter;

    @Inject
    User user;

    @BindView(R.id.btnContinue)
    View btnContinue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        btnContinue.setEnabled(false);
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        hideLoading();
        super.onDestroyView();
    }

    public void showLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    public void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_TRANSFER:
                    getActivity().finish();
                    break;
            }
        }
    }

    @Override
    public void onGetProfileSuccess(Person person, String zaloPayName) {
        RecentTransaction item = new RecentTransaction();
        item.avatar = person.avatar;
        item.zaloPayId = person.zaloPayId;
        item.displayName = person.displayName;
        item.phoneNumber = String.valueOf(person.phonenumber);
        item.zaloPayName = zaloPayName;

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, Parcels.wrap(item));
        navigator.startTransferActivity(this, bundle);
    }

    @Override
    public void showError(String message) {
        textInputAccountName.setErrorEnabled(!TextUtils.isEmpty(message));
        textInputAccountName.setError(message);
    }

    private SweetAlertDialog mProgressDialog;

    @OnClick(R.id.btnContinue)
    public void onClickContinue() {
        String s = textInputAccountName.getEditText().getText().toString().trim();
        Timber.d("name %s", s);

        boolean isValidExt = isValidExt(s);
        if (!isValidExt) {
            return;
        }
        boolean isValid = isValidChanged(s);
        if (!isValid) {
            return;
        }

        presenter.getUserInfo(s);
    }

    @OnTextChanged(R.id.edtAccountName)
    public void onTextChanged(CharSequence s) {


        boolean isValid = isValidChanged(s.toString());

        Timber.d("onTextChanged: s %s isValid %s", s, isValid);

        if (isValid) {
            showError(null);
        }

        btnContinue.setEnabled(isValid && ValidateUtil.isValidLengthZPName(s.toString()));
    }

    private boolean isValidExt(String s) {
        if (s.length() == 0) {
            showError(getContext().getString(R.string.exception_empty_account));
            return false;
        } else if (!ValidateUtil.isValidLengthZPName(s)) {
            showError(getContext().getString(R.string.exception_account_name_length));
            return false;
        } else if (s.equals(user.zalopayname)) {
            showError(getContext().getString(R.string.exception_transfer_for_self));
            return false;
        }

        return true;
    }

    private boolean isValidChanged(String s) {
        if (s.indexOf(" ") > 0) {
            showError(getContext().getString(R.string.exception_account_name_with_space));
            return false;
        } else if (!ValidateUtil.isValidZaloPayName(s)) {
            showError(getContext().getString(R.string.exception_account_name_special_char));
            return false;
        }

        return true;
    }

}
