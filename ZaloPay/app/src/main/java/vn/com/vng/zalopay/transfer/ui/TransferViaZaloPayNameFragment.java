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
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.view.ITransferMoneyView;
import vn.com.vng.zalopay.ui.widget.validate.MinCharactersValidate;
import vn.com.vng.zalopay.ui.widget.validate.SpecialCharactersValidate;

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

    @Inject
    TransferViaZaloPayNamePresenter presenter;

    @Inject
    User user;

    @BindView(R.id.btnContinue)
    View mBtnContinue;

    @BindView(R.id.edtAccountName)
    ZPEditText mEdtAccountNameView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);
        mEdtAccountNameView.addValidator(new MinCharactersValidate(getString(R.string.exception_account_name_length), 4));
        mEdtAccountNameView.addValidator(new ZPEditTextValidate(getString(R.string.exception_transfer_for_self)) {
            @Override
            public boolean isValid(@NonNull CharSequence s) {
                return !s.toString().equals(user.zalopayname);
            }
        });
        mEdtAccountNameView.addValidator(new SpecialCharactersValidate(getString(R.string.exception_account_name_special_char)));
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
        RecentTransaction item = new RecentTransaction();
        item.avatar = person.avatar;
        item.zaloPayId = person.zaloPayId;
        item.displayName = person.displayName;
        item.phoneNumber = String.valueOf(person.phonenumber);
        item.zaloPayName = zaloPayName;

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
        navigator.startTransferActivity(this, bundle);
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

        presenter.getUserInfo(mEdtAccountNameView.getText().toString().trim());
    }

    @OnTextChanged(value = R.id.edtAccountName, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTextChanged(CharSequence s) {
        mBtnContinue.setEnabled(mEdtAccountNameView.isValid());
    }
}
