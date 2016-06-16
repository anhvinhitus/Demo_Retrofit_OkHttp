package vn.com.vng.zalopay.transfer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
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
import vn.com.vng.zalopay.utils.VNDCurrencyTextWatcher;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TransferFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransferFragment extends BaseFragment implements ITransferView {
    private OnFragmentInteractionListener mListener;

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
        btnContinue.setEnabled(!TextUtils.isEmpty(charSequence));
    }

    public boolean isValidAmount() {
        String amount = edtAmount.getText().toString();
        if (TextUtils.isEmpty(amount) || mAmount <= 0) {
            return false;
        }
        return true;
    }

    private void showAmountError() {
        textInputAmount.setErrorEnabled(true);
        if (TextUtils.isEmpty(edtAmount.getText().toString())) {
            textInputAmount.setError(getString(R.string.invalid_amount_empty));
        }
    }

    private void hideAmountError() {
        textInputAmount.setErrorEnabled(false);
        textInputAmount.setError(null);
    }

//    public boolean isValidTransferMsg() {
//        String transferMsg = edtTransferMsg.getText().toString();
//        if (TextUtils.isEmpty(transferMsg)) {
//            return false;
//        }
//        return true;
//    }
//
//    private void showTransferMsgError() {
//        textInputTransferMsg.setErrorEnabled(true);
//        if (TextUtils.isEmpty(edtTransferMsg.getText().toString())) {
//            textInputTransferMsg.setError(getString(R.string.invalid_transfer_msg_empty));
//        }
//    }
//
//    private void hideTransferMsgError() {
//        textInputAmount.setErrorEnabled(false);
//        textInputAmount.setError(null);
//    }

    @BindView(R.id.btnContinue)
    View btnContinue;

    @OnClick(R.id.btnContinue)
    public void onClickContinute(View view) {
        if (userMapZaloAndZaloPay == null || userMapZaloAndZaloPay.getZaloId() <= 0) {
            showToast("Thông tin tài khoản cần chuyển tiền không chính xác");
            return;
        }
//        if (isValidAmount()) {
//            showAmountError();
//        } else {
//            hideAmountError();
//        }
//        if (isValidTransferMsg()) {
//            showTransferMsgError();
//        } else {
//            hideTransferMsgError();
//        }
        if (edtTransferMsg == null) {
            return;
        }
        if (zaloFriend == null) {
            return;
        }
//        String phoneNumber = "";
//        String appUser = "";
//        if (userMapZaloAndZaloPay != null) {
//            appUser = userMapZaloAndZaloPay.getZaloPayId();
//            phoneNumber = userMapZaloAndZaloPay.getPhonenumber();
//        }
        mPresenter.transferMoney(mAmount, edtTransferMsg.getText().toString(), zaloFriend, userMapZaloAndZaloPay);
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
        btnContinue.setEnabled(edtAmount.length() > 0);
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
            mPresenter.getUserMapping(zaloFriend.getUserId());
        }

        initCurrentState();
    }

    private void initCurrentState() {
        if (!TextUtils.isEmpty(mMessage)) {
            edtTransferMsg.setText(mMessage);
        }
        if (mAmount > 0) {
            edtAmount.setText(String.valueOf(mAmount));
            edtAmount.setSelection(edtAmount.getText().toString().length());
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    public void updateUserPhone(MappingZaloAndZaloPay userMapZaloAndZaloPay) {
        if (userMapZaloAndZaloPay == null) {
            return;
        }
        this.userMapZaloAndZaloPay = userMapZaloAndZaloPay;
        tvPhone.setText(this.userMapZaloAndZaloPay.getPhonenumber());
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
