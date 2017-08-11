package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.zalopay.ui.widget.edittext.ZPEditText;
import com.zalopay.ui.widget.edittext.ZPEditTextValidate;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.transfer.model.TransferMode;
import vn.com.vng.zalopay.transfer.model.TransferObject;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.view.ITransferMoneyView;
import vn.com.vng.zalopay.ui.widget.validate.MinCharactersValidate;
import vn.com.vng.zalopay.ui.widget.validate.SpecialCharactersValidate;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
public class TransferViaZaloPayNameFragment extends BaseFragment implements ITransferMoneyView {

    @Inject
    TransferViaZaloPayNamePresenter presenter;
    @Inject
    User user;
    @BindView(R.id.btnContinue)
    View mBtnContinue;
    @BindView(R.id.edtAccountName)
    ZPEditText mEdtAccountNameView;

    public static TransferViaZaloPayNameFragment newInstance(String transferMode) {

        Bundle args = new Bundle();
        if (transferMode != null) {
            args.putString(Constants.TRANSFER_MODE, transferMode);
        }

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);

        if (isTransferViaPN()) {
            mEdtAccountNameView.setHint("Số điện thoại");
            // TODO: code here for init Validator editText
        } else {
            mEdtAccountNameView.addValidator(new MinCharactersValidate(getString(R.string.exception_account_name_length), 4));
            mEdtAccountNameView.addValidator(new ZPEditTextValidate(getString(R.string.exception_transfer_for_self)) {
                @Override
                public boolean isValid(@NonNull CharSequence s) {
                    return !s.toString().equals(user.zalopayname);
                }
            });
            mEdtAccountNameView.addValidator(new SpecialCharactersValidate(getString(R.string.exception_account_name_special_char)));
        }

        mBtnContinue.setEnabled(mEdtAccountNameView.isValid());
    }

    @Override
    public void onDestroyView() {
        mEdtAccountNameView.clearValidators();
        presenter.detachView();
        hideLoading();
        super.onDestroyView();
    }

    public void showLoading() {
        showProgressDialog();
    }

    public void hideLoading() {
        hideProgressDialog();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_TRANSFER:
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                    break;
            }
        }
    }

    @Override
    public void onGetProfileSuccess(Person person, String zaloPayName) {
        showError(null);

        Timber.d("Got profile for %s: %s", zaloPayName, person);
        TransferObject object = new TransferObject(person);
        navigator.startTransferActivity(this, object, Constants.REQUEST_CODE_TRANSFER);
    }

    @Override
    public boolean isTransferViaPN() {
        if (getArguments() != null && getArguments().containsKey(Constants.TRANSFER_MODE)) {
            String str = getArguments().getString(Constants.TRANSFER_MODE);
            if (str != null && str.equals(TransferMode.PHONE_NUMBER))
                return true;
        }
        return false;
    }

    @Override
    public void showError(String message) {
        mEdtAccountNameView.setError(message);
    }

    @OnClick(R.id.btnContinue)
    public void onClickContinue() {
        if (!mEdtAccountNameView.validate()) {
            return;
        }

        if (isTransferViaPN()) {
            presenter.getPhoneNumberInfo(mEdtAccountNameView.getText().toString().trim());
        } else {
            presenter.getUserInfo(mEdtAccountNameView.getText().toString().trim());
        }
    }

    @OnTextChanged(value = R.id.edtAccountName, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTextChanged(CharSequence s) {
        mBtnContinue.setEnabled(mEdtAccountNameView.isValid());
    }
}
