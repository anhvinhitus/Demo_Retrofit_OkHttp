package vn.com.vng.zalopay.authentication;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import vn.com.vng.zalopay.ui.widget.GridPasswordViewFitWidth;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;
import vn.com.zalopay.wallet.view.custom.pinview.GridPasswordView;


public class AuthenticationDialog extends DialogFragment implements IAuthenticationView {


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

    @Inject
    AuthenticationPresenter mPresenter;

    private Intent pendingIntent;

    private Stage mStage = Stage.FINGERPRINT_DECRYPT;

    private boolean isFinish = false; //

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
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        setupFragmentComponent();
        return v;
    }

    private void setupFragmentComponent() {
        AndroidApplication.instance().getUserComponent().inject(this);
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
        mPassword.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String s) {

            }

            @Override
            public void onInputFinish(String s) {
                Timber.d("onInputFinish: %s", s);
                mPresenter.verify(s);
                mPassword.clearPassword();
            }
        });

    }

    @OnClick(R.id.second_dialog_button)
    public void onOnClickButtonSecond(View v) {
        if (mPresenter.getStage() == Stage.PASSWORD) {
            dismiss();
            return;
        }

        mPresenter.verify(mPassword.getPassWord());
        mPassword.clearPassword();
    }

    @OnClick(R.id.cancel_button)
    public void onClickCancel(View v) {
        if (mCallback != null) {
            mCallback.onCancel();
        }
        dismiss();
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
                mCancelButton.setVisibility(View.VISIBLE);
                mSecondDialogButton.setText(R.string.use_password);
                mSecondDialogButton.setVisibility(mVisibleSecondButton ? View.VISIBLE : View.GONE);
                mFingerprintDecrypt.setVisibility(View.VISIBLE);
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

        if (mCallback != null) {
            mCallback.onAuthenticated(password);
        }

        dismiss();
    }

    @Override
    public void onAuthenticationFailure() {
        Timber.d("onAuthenticationFailure");
        if (mCallback != null) {
            mCallback.onAuthenticationFailure();
        }

        dismiss();
    }

    @Override
    public void showFingerprintError(CharSequence error) {
        mTvDecryptView.setText(error);
        mTvDecryptView.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
        // mIcon.setImageResource(R.drawable.ic_fingerprint_error);

        AndroidUtils.cancelRunOnUIThread(mResetErrorTextRunnable);
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

            mTvDecryptView.setTextColor(ContextCompat.getColor(getActivity(), R.color.hint));
            mTvDecryptView.setText(getString(R.string.fingerprint_hint));
            // mIcon.setImageResource(R.drawable.ic_touch);
        }
    };

    private boolean mVisibleSecondButton = true;

    public void setVisibleSecondButton(boolean visibleSecondButton) {
        this.mVisibleSecondButton = visibleSecondButton;
    }
}
