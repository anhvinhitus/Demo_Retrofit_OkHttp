package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.RecoveryPinPresenter;
import vn.com.vng.zalopay.account.ui.view.IRecoveryPinView;
import vn.com.vng.zalopay.ui.widget.IPasscodeChanged;
import vn.com.vng.zalopay.ui.widget.PassCodeView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChangePinFragment.OnPinProfileFragmentListener} interface
 * to handle interaction events.
 * Use the {@link ChangePinFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangePinFragment extends AbsProfileFragment implements IRecoveryPinView {
    private OnPinProfileFragmentListener mListener;

    @Inject
    RecoveryPinPresenter presenter;

    @BindView(R.id.passcodeInput)
    PassCodeView passCode;

    IPasscodeChanged passCodeChanged = new IPasscodeChanged() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isValidPin()) {
                passCode.hideError();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private boolean isValidPin() {
        String pin = passCode.getText();
        return !TextUtils.isEmpty(pin) && pin.length() == passCode.getMaxLength();
    }

    public ChangePinFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PinProfileFragment.
     */
    public static ChangePinFragment newInstance() {
        return new ChangePinFragment();
    }

    @Override
    public void onClickContinue() {
        if (!isValidPin()) {
            passCode.showError(getString(R.string.invalid_pin));
            return;
        } else {
            passCode.hideError();
        }

        presenter.updateProfile(passCode.getText(), null);
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_change_pin;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        passCode.setPasscodeChanged(passCodeChanged);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPinProfileFragmentListener) {
            mListener = (OnPinProfileFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
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

    @Override
    public void onRecoveryPinSuccess() {
        if (mListener != null) {
            mListener.onUpdatePinSuccess();
        }
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
    public interface OnPinProfileFragmentListener {
        void onUpdatePinSuccess();

        void onUpdatePinFail();
    }
}
