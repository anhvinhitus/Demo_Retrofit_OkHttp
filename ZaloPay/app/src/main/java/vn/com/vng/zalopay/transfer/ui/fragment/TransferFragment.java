package vn.com.vng.zalopay.transfer.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TransferFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransferFragment extends BaseFragment {
    private OnFragmentInteractionListener mListener;

    private ZaloFriend zaloFriend;

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

    public boolean isValidAmount() {
        String amount = edtAmount.getText().toString();
        if (TextUtils.isEmpty(amount)) {
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

    public boolean isValidTransferMsg() {
        String transferMsg = edtTransferMsg.getText().toString();
        if (TextUtils.isEmpty(transferMsg)) {
            return false;
        }
        return true;
    }

    private void showTransferMsgError() {
        textInputTransferMsg.setErrorEnabled(true);
        if (TextUtils.isEmpty(edtTransferMsg.getText().toString())) {
            textInputTransferMsg.setError(getString(R.string.invalid_transfer_msg_empty));
        }
    }

    private void hideTransferMsgError() {
        textInputAmount.setErrorEnabled(false);
        textInputAmount.setError(null);
    }

    @OnClick(R.id.btnContinue)
    public void onClickContinute(View view) {
        if (isValidAmount()) {
            showAmountError();
        } else {
            hideAmountError();
        }
        if (isValidTransferMsg()) {
            showTransferMsgError();
        } else {
            hideTransferMsgError();
        }

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

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
