package vn.com.vng.zalopay.transfer.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.transfer.ui.presenter.TransferPresenter;
import vn.com.vng.zalopay.transfer.ui.view.ITransferView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.VNDCurrencyTextWatcher;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;


public class TransferFragment2 extends BaseFragment implements ITransferView {

    public static TransferFragment2 newInstance(Bundle args) {
        TransferFragment2 fragment = new TransferFragment2();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_transfer;
    }

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.tvDisplayName)
    TextView tvDisplayName;

    @BindView(R.id.tvZaloPayName)
    TextView mTextViewZaloPayName;

    @BindView(R.id.textInputTransferMsg)
    TextInputLayout textInputTransferMsg;

    @BindView(R.id.textInputAmount)
    TextInputLayout textInputAmount;

    @BindView(R.id.btnContinue)
    View btnContinue;

    @BindView(R.id.edtAmount)
    EditText edtAmount;

    Person person;

    private long mAmount = 0;
    private String mValidMinAmount = "";
    private String mValidMaxAmount = "";


    @Inject
    TransferPresenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        person = getArguments().getParcelable("person");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUserInfo(person);

        edtAmount.addTextChangedListener(new VNDCurrencyTextWatcher(edtAmount) {
            @Override
            public void onValueUpdate(long value) {
                mAmount = value;
            }

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                hideAmountError();
                isValidMaxAmount();
                checkShowBtnContinue();
            }
        });

        mPresenter.setView(this);
        checkShowBtnContinue();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (TextUtils.isEmpty(person.uid)) {
            mPresenter.getUserMapping(person.zaloId);
        }
    }

    @Override
    public void onDestroyView() {
        mPresenter.destroyView();
        super.onDestroyView();

    }

    public void setUserInfo(Person person) {
        tvDisplayName.setText(person.dname);
        setAvatar(person.avatar);
        setZaloPayName(person.zalopayname);
    }

    public void setAvatar(String url) {
        Glide.with(this).load(url)
                .placeholder(R.color.silver)
                .error(R.drawable.ic_avatar_default)
                .centerCrop()
                .into(imgAvatar);
    }

    public void setZaloPayName(String zaloPayName) {
        if (TextUtils.isEmpty(zaloPayName)) {
            zaloPayName = getString(R.string.not_update_zalopayname);
        }
        mTextViewZaloPayName.setText(zaloPayName);
    }

    @Override
    public void onTokenInvalid() {
    }

    @Override
    public void setEnableBtnContinue(boolean isEnable) {
        btnContinue.setEnabled(isEnable);
    }

    private void checkShowBtnContinue() {
        btnContinue.setEnabled(mAmount > 0 && !TextUtils.isEmpty(person.uid));
    }

    @Override
    public void onGetMappingUserSuccess(MappingZaloAndZaloPay userMapZaloAndZaloPay) {
        person.uid = userMapZaloAndZaloPay.getZaloPayId();
        try {
            person.phonenumber = Long.valueOf(userMapZaloAndZaloPay.phonenumber);
        } catch (Exception ex) {
        }

        setUserInfo(person);
    }

    @Override
    public void onGetMappingUserError() {
        showErrorDialog(getString(R.string.get_mapping_zalo_zalopay_error), getString(R.string.txt_close), new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.cancel();
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

    @Override
    public void showRetry() {
    }

    @Override
    public void hideRetry() {
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    public boolean isValidMinAmount() {
        if (mAmount < Constants.MIN_TRANSFER_MONEY) {
            showAmountError(mValidMinAmount);
            return false;
        }
        return true;
    }

    public boolean isValidMaxAmount() {
        if (mAmount > Constants.MAX_TRANSFER_MONEY) {
            showAmountError(mValidMaxAmount);
            return false;
        }
        return true;
    }

    public boolean isValidAmount() {
        if (!isValidMinAmount()) {
            return false;
        }

        return isValidMaxAmount();

    }

    private void showAmountError(String error) {
        if (!TextUtils.isEmpty(error)) {
            textInputAmount.setErrorEnabled(true);
            textInputAmount.setError(error);
        } else {
            hideAmountError();
        }
    }

    private void hideAmountError() {
        textInputAmount.setErrorEnabled(false);
        textInputAmount.setError(null);
    }

    @OnClick(R.id.btnContinue)
    public void onClickContinue() {
        if (!isValidAmount()) {
            return;
        }
        mPresenter.transferMoney(mAmount, textInputTransferMsg.getEditText().getText().toString(), person);
        setEnableBtnContinue(false);
    }


}
