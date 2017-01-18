package vn.com.vng.zalopay.authentication;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.ui.widget.GridPasswordViewFitWidth;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;
import vn.com.zalopay.wallet.view.custom.pinview.GridPasswordView;


public class AuthenticationDialog extends DialogFragment implements IAuthenticationView, GridPasswordView.OnPasswordChangedListener {


    public static AuthenticationDialog newInstance() {

        Bundle args = new Bundle();

        AuthenticationDialog fragment = new AuthenticationDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static final String TAG = "FingerprintDialog";

    public void setPendingIntent(Intent intent) {
        this.pendingIntent = intent;
    }

    public void setStage(Stage stage) {
        mStage = stage;
    }

    public void setAuthenticationCallback(AuthenticationCallback callback) {
        this.mCallback = callback;
    }

    /**
     * Finish Activity sau khi XÁC THỰC THÀNH CÔNG
     */
    public void setFinishActivity(boolean isFinish) {
        this.isFinish = isFinish;
    }

    @BindView(R.id.cancel_button)
    Button mCancelButton;

    @BindView(R.id.second_dialog_button)
    Button mSecondDialogButton;

    @BindView(R.id.fingerprint_decrypt_container)
    View mFingerprintDecrypt;

    @BindView(R.id.backup_container)
    View mBackupContent;

    @BindView(R.id.password)
    GridPasswordViewFitWidth mPassword;

    @BindView(R.id.hintPassword)
    TextView mHintPassword;

    @BindView(R.id.fingerprint_status_decrypt)
    TextView mTvDecryptView;

    private Unbinder mUnbinder;

    private AuthenticationCallback mCallback;

    @BindView(R.id.password_description)
    TextView mPasswordDescriptionView;

    @Inject
    AuthenticationPresenter mPresenter;

    private Intent pendingIntent;

    private Stage mStage = Stage.FINGERPRINT_DECRYPT;

    private boolean isFinish = false; //

    private boolean isCancel = true;

    public AuthenticationDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.alert_dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.confirm));
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        setupFragmentComponent();
        return v;
    }

    private void setupFragmentComponent() {
        UserComponent userComponent = AndroidApplication.instance().getUserComponent();
        if (userComponent != null) {
            userComponent.inject(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setWidthDialog();
    }

    private void setWidthDialog() {
        if (getDialog() == null) {
            return;
        }

        Window window = getDialog().getWindow();
        if (window == null) {
            return;
        }

        int widthScreen = AndroidUtils.displaySize.x;
        int width = (int) (widthScreen * 0.85D);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        window.setLayout(width, height);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mPresenter.setStage(mStage);
        mPresenter.onViewCreated();
        mPassword.setOnPasswordChangedListener(this);

        if (!TextUtils.isEmpty(mMessagePassword)) {
            mPasswordDescriptionView.setText(mMessagePassword);
        }
    }


    @Override
    public void onTextChanged(String s) {
        //empty
    }

    @Override
    public void onInputFinish(String s) {
        Timber.d("onInputFinish: %s", s);
        mPresenter.verify(s);
    }

    @OnClick(R.id.second_dialog_button)
    public void onOnClickButtonSecond(View v) {
        if (mPresenter.getStage() == Stage.PASSWORD) {
            cancel();
            return;
        }

        if (isAuthPayment) {
            onAuthenticated("");
            return;
        }

        mPresenter.verify(mPassword.getPassWord());
        mPassword.clearPassword();
    }

    private void cancel() {
        isCancel = true;
        dismiss();
    }

    @OnClick(R.id.cancel_button)
    public void onClickCancel(View v) {
        cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        mPassword.setOnPasswordChangedListener(null);
        mPresenter.detachView();
        mUnbinder.unbind();
        super.onDestroyView();
    }

    private final Runnable mShowKeyboardRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPassword != null) {
                mPassword.forceInputViewGetFocus();
            }
        }
    };

    public void updateStage(Stage mStage) {
        switch (mStage) {
            case FINGERPRINT_DECRYPT:
                mCancelButton.setText(R.string.cancel);
                mCancelButton.setVisibility(isAuthPayment ? View.GONE : View.VISIBLE);
                mSecondDialogButton.setText(R.string.use_password);
                mSecondDialogButton.setVisibility(View.VISIBLE);
                mFingerprintDecrypt.setVisibility(View.VISIBLE);
                mTvDecryptView.setText(isAuthPayment ? R.string.fingerprint_description_pay : R.string.fingerprint_status_decrypt);
                mBackupContent.setVisibility(View.GONE);
                break;

            case PASSWORD:
                mCancelButton.setText(R.string.cancel);
                mCancelButton.setVisibility(View.INVISIBLE);
                mSecondDialogButton.setText(R.string.txt_close);
                mFingerprintDecrypt.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void clearPassword() {
        mPassword.clearPassword();
    }

    @Override
    public void showKeyboard() {
        if (mPassword != null) {
            mPassword.postDelayed(mShowKeyboardRunnable, 250);
        }
    }

    @Override
    public void showLoading() {
        DialogHelper.showLoading(getActivity(), null);
    }

    @Override
    public void hideLoading() {
        DialogHelper.hideLoading();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setErrorVerifyPassword(String error) {
        if (mHintPassword != null) {
            mHintPassword.setText(error);
        }
    }

    @Override
    public void showNetworkErrorDialog() {
        //empty
    }

    @Override
    public void showNetworkErrorDialog(ZPWOnSweetDialogListener listener) {
        //empty
    }

    @Override
    public void onAuthenticated(String password) {
        Timber.d("onAuthenticated");

        if (pendingIntent != null) {
            startActivity(pendingIntent);
            if (isFinish) {
                getActivity().finish();
            }
        }

        isCancel = false;
        if (mCallback != null) {
            mCallback.onAuthenticated(password);
        }

        dismiss();
    }

    @Override
    public void onAuthenticationFailure() {
        Timber.d("onAuthenticationFailure");

        isCancel = false;
        if (mCallback != null) {
            mCallback.onAuthenticationFailure();
        }

        dismiss();
    }

    @Override
    public void showFingerprintError(CharSequence error, boolean retry) {
        mTvDecryptView.setText(error);
        mTvDecryptView.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
        // mIcon.setImageResource(R.drawable.ic_fingerprint_error);

        AndroidUtils.cancelRunOnUIThread(mResetErrorTextRunnable);

        if (!retry) {
            return;
        }
        AndroidUtils.runOnUIThread(mResetErrorTextRunnable, FingerprintProvider.ERROR_TIMEOUT_MILLIS);
    }

    @Override
    public void showFingerprintSuccess() {

        AndroidUtils.cancelRunOnUIThread(mResetErrorTextRunnable);
        mTvDecryptView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        mTvDecryptView.setText(getString(R.string.fingerprint_success));
        //  mIcon.setImageResource(R.drawable.ic_fingerprint_success);
    }

    Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTvDecryptView == null) {
                return;
            }

            mTvDecryptView.setTextColor(ContextCompat.getColor(getActivity(), R.color.hint));
            mTvDecryptView.setText(getString(R.string.fingerprint_hint));
            // mIcon.setImageResource(R.drawable.ic_touch);
        }
    };

    private boolean isAuthPayment = false;

    public void setContentPayment(boolean isAuthPayment) {
        this.isAuthPayment = isAuthPayment;
    }

    private String mMessagePassword;

    public void setMessagePassword(String message) {
        mMessagePassword = message;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Timber.d("onDismiss: %s", isCancel);
        AndroidUtils.cancelRunOnUIThread(mResetErrorTextRunnable);
        if (mCallback != null && isCancel) {
            mCallback.onCancel();
        }
        super.onDismiss(dialog);
    }
}
