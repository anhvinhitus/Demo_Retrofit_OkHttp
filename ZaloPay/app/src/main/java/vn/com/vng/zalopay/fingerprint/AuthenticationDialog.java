package vn.com.vng.zalopay.fingerprint;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.widget.GridPasswordViewFitWidth;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;
import vn.com.zalopay.wallet.view.custom.pinview.GridPasswordView;


public class AuthenticationDialog extends DialogFragment implements IFingerprintAuthenticationView {


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

    @BindView(R.id.rootView)
    View mRootView;

    @BindView(R.id.cancel_button)
    Button mCancelButton;

    @BindView(R.id.second_dialog_button)
    Button mSecondDialogButton;

    @BindView(R.id.fingerprint_encrypt_container)
    View mFingerprintEncrypt;

    @BindView(R.id.fingerprint_decrypt_container)
    View mFingerprintDecrypt;

    @BindView(R.id.backup_container)
    View mBackupContent;

    @BindView(R.id.password)
    GridPasswordViewFitWidth mPassword;

    @BindView(R.id.hintPassword)
    TextView mHintPassword;

    @BindView(R.id.fingerprint_icon_decrypt)
    ImageView mIconDecryptView;

    @BindView(R.id.fingerprint_status_decrypt)
    TextView mTvDecryptView;

    @BindView(R.id.fingerprint_icon)
    ImageView mIconFingerprintView;

    @BindView(R.id.fingerprint_status)
    TextView mStatusFingerprint;

    private Unbinder mUnbinder;

    private AuthenticationCallback mCallback;

    @Inject
    FingerAuthenticationPresenter mPresenter;

    @Inject
    FingerprintUiHelper.FingerprintUiHelperBuilder mFingerprintUiHelperBuilder;

    private Intent pendingIntent;

    private Stage mStage = Stage.FINGERPRINT_DECRYPT;

    private boolean isFinish = false; //

    @Inject
    Navigator mNavigator;

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
        if (mPresenter.getStage() == Stage.PASSWORD ||
                mPresenter.getStage() == Stage.PASSWORD_SETTING) {
            dismiss();
            return;
        }

        mPresenter.verify(mPassword.getPassWord());
        mPassword.clearPassword();
    }

    @OnClick(R.id.cancel_button)
    public void onClickCancel(View v) {
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
                mFingerprintDecrypt.setVisibility(View.VISIBLE);
                mFingerprintEncrypt.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.GONE);
                break;

            case PASSWORD:
                mCancelButton.setText(R.string.cancel);
                mCancelButton.setVisibility(View.INVISIBLE);

                mSecondDialogButton.setText(R.string.txt_close);
                mFingerprintEncrypt.setVisibility(View.GONE);
                mFingerprintDecrypt.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.VISIBLE);
                break;
            case PASSWORD_SETTING:
                mCancelButton.setText(R.string.cancel);
                mCancelButton.setVisibility(View.INVISIBLE);

                mSecondDialogButton.setText(R.string.txt_close);
                mFingerprintEncrypt.setVisibility(View.GONE);
                mFingerprintDecrypt.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.VISIBLE);
                break;
            case FINGERPRINT_ENCRYPT:
                mCancelButton.setText(R.string.cancel);
                mCancelButton.setVisibility(View.VISIBLE);
                mSecondDialogButton.setVisibility(View.GONE);
                mFingerprintEncrypt.setVisibility(View.VISIBLE);
                mFingerprintDecrypt.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.GONE);
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
    public void onAuthenticated() {
        Timber.d("onAuthenticated");
        if (pendingIntent != null) {
            startActivity(pendingIntent);
            if (isFinish) {
                getActivity().finish();
            }
        } else if (mCallback != null) {
            mCallback.onAuthenticated();
        }
        mNavigator.setLastTimeCheckPin(System.currentTimeMillis());
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
    public FingerprintUiHelper getFingerprintUiHelper(Stage stage) {
        switch (stage) {
            case FINGERPRINT_ENCRYPT:
                return mFingerprintUiHelperBuilder.build(mIconFingerprintView, mStatusFingerprint, mPresenter);
            case FINGERPRINT_DECRYPT:
                return mFingerprintUiHelperBuilder.build(mIconDecryptView, mTvDecryptView, mPresenter);
            default:
                return mFingerprintUiHelperBuilder.build(mIconDecryptView, mTvDecryptView, mPresenter);
        }

    }

}
