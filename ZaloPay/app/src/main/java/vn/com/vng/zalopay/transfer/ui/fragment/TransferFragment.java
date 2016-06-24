package vn.com.vng.zalopay.transfer.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.transfer.models.TransferRecent;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;
import vn.com.vng.zalopay.transfer.ui.presenter.TransferPresenter;
import vn.com.vng.zalopay.transfer.ui.view.ITransferView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.PhoneUtil;
import vn.com.vng.zalopay.utils.VNDCurrencyTextWatcher;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransferFragment extends BaseFragment implements ITransferView {
    private MappingZaloAndZaloPay userMapZaloAndZaloPay;
    private ZaloFriend zaloFriend;
    private long mAmount = 0;
    private String mMessage = "";

    @Inject
    TransferPresenter mPresenter;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.tvDisplayName)
    TextView tvDisplayName;

    @BindView(R.id.tvPhone)
    TextView tvPhone;

    @BindView(R.id.textInputAmount)
    TextInputLayout textInputAmount;

    @BindView(R.id.edtAmount)
    EditText edtAmount;

    @BindView(R.id.textInputTransferMsg)
    TextInputLayout textInputTransferMsg;

    @BindView(R.id.edtTransferMsg)
    EditText edtTransferMsg;

    @OnTextChanged(R.id.edtAmount)
    public void onTextChangedAmount(CharSequence charSequence) {
        checkShowBtnContinue();
    }

    @BindView(R.id.btnContinue)
    View btnContinue;

    @OnClick(R.id.btnContinue)
    public void onClickContinute() {
        if (edtTransferMsg == null) {
            return;
        }
        if (zaloFriend == null) {
            return;
        }
        mPresenter.transferMoney(mAmount, edtTransferMsg.getText().toString(), zaloFriend, userMapZaloAndZaloPay);
        setEnableBtnContinue(false);
    }

    @Override
    public void setEnableBtnContinue(boolean isEnable) {
        btnContinue.setEnabled(isEnable);
    }

    public TransferFragment() {
        // Required empty public constructor
    }

    public static TransferFragment newInstance(Bundle bundle) {
        TransferFragment fragment = new TransferFragment();
        fragment.setArguments(bundle);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            zaloFriend = getArguments().getParcelable(Constants.ARG_ZALO_FRIEND);
            mMessage = getArguments().getString(Constants.ARG_MESSAGE);
            mAmount = getArguments().getLong(Constants.ARG_AMOUNT);
            TransferRecent transferRecent = getArguments().getParcelable(Constants.ARG_TRANSFERRECENT);
            if (transferRecent != null && zaloFriend == null) {
                zaloFriend = new ZaloFriend();
                zaloFriend.setUserId(transferRecent.getUserId());
                zaloFriend.setDisplayName(transferRecent.getDisplayName());
                zaloFriend.setUserName(transferRecent.getUserName());
                zaloFriend.setAvatar(transferRecent.getAvatar());
                zaloFriend.setUserGender(transferRecent.getUserGender());
                zaloFriend.setUsingApp(transferRecent.isUsingApp());

                userMapZaloAndZaloPay = new MappingZaloAndZaloPay(transferRecent.getUserId(), transferRecent.getZaloPayId(), transferRecent.getPhoneNumber());

                mAmount = transferRecent.getAmount();
                mMessage = transferRecent.getMessage();
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnContinue.setEnabled(false);
        mPresenter.setView(this);
        edtAmount.addTextChangedListener(new VNDCurrencyTextWatcher(edtAmount) {
            @Override
            public void onValueUpdate(long value) {
                mAmount = value;
            }

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                showError(null);
            }
        });
        Timber.tag(TAG).d("onViewCreated zaloFriend: %s", zaloFriend);
        if (zaloFriend != null) {
            Timber.tag(TAG).d("onViewCreated zaloFriend.uid:%s", zaloFriend.getUserId());
            updateUserInfo(zaloFriend);
            if (userMapZaloAndZaloPay == null ||
                    TextUtils.isEmpty(userMapZaloAndZaloPay.getZaloPayId()) ||
                    userMapZaloAndZaloPay.getZaloId() != zaloFriend.getUserId()) {
                mPresenter.getUserMapping(zaloFriend.getUserId());
            }
        }

        initCurrentState();
        checkShowBtnContinue();
    }

    private void checkShowBtnContinue() {
        if (mAmount <= 0) {
            return;
        }
        if (userMapZaloAndZaloPay == null ||
                TextUtils.isEmpty(userMapZaloAndZaloPay.getZaloPayId()) ||
                userMapZaloAndZaloPay.getZaloId() != zaloFriend.getUserId()) {
            return;
        }
        btnContinue.setEnabled(true);
    }

    private void initCurrentState() {
        if (!TextUtils.isEmpty(mMessage)) {
            edtTransferMsg.setText(mMessage);
        }
        if (mAmount > 0) {
            edtAmount.setText(String.valueOf(mAmount));
            edtAmount.setSelection(edtAmount.getText().toString().length());
        }
        showPhoneNumber();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onDestroyView() {
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.ARG_AMOUNT, mAmount);
        intent.putExtra(Constants.ARG_MESSAGE, edtTransferMsg.getText().toString());
        getActivity().setResult(Activity.RESULT_CANCELED, intent);
        getActivity().finish();
        return true;
    }

    private void updateUserInfo(ZaloFriend zaloFriend) {
        if (zaloFriend == null) {
            return;
        }
        tvDisplayName.setText(zaloFriend.getDisplayName());
        Glide.with(this).load(zaloFriend.getAvatar())
                .placeholder(R.color.silver)
                .centerCrop()
                .into(imgAvatar);
    }

    @Override
    public void onTokenInvalid() {
    }

    @Override
    public void onGetMappingUserSucess(MappingZaloAndZaloPay userMapZaloAndZaloPay) {
        if (userMapZaloAndZaloPay == null) {
            return;
        }
        this.userMapZaloAndZaloPay = userMapZaloAndZaloPay;
        showPhoneNumber();
        checkShowBtnContinue();
    }

    private void showPhoneNumber() {
        Timber.d("showPhoneNumber userMapZaloAndZaloPay:%s", userMapZaloAndZaloPay);
        if (userMapZaloAndZaloPay == null) {
            return;
        }
        String phoneNumber = PhoneUtil.formatPhoneNumber(userMapZaloAndZaloPay.getPhonenumber());
        if (PhoneUtil.isPhoneNumber(phoneNumber)) {
            tvPhone.setText(phoneNumber);
        } else {
            tvPhone.setText(getString(R.string.not_update_phone));
        }
    }

    @Override
    public void onGetMappingUserError() {
        showErrorDialog(getString(R.string.get_mapping_zalo_zalopay_error), getString(R.string.txt_close), new SweetAlertDialog.OnSweetClickListener() {

            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                getActivity().finish();
                sweetAlertDialog.cancel();
            }
        });
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
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        showToast(message);
    }
}
