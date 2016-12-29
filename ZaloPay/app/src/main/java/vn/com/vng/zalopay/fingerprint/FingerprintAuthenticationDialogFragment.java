/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
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
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.widget.GridPasswordViewFitWidth;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;
import vn.com.zalopay.wallet.view.custom.pinview.GridPasswordView;


public class FingerprintAuthenticationDialogFragment extends DialogFragment implements IFingerprintAuthenticationView {


    public static final String TAG = "FingerprintDialog";


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
    InputMethodManager mInputMethodManager;

    @Inject
    FingerprintUiHelper.FingerprintUiHelperBuilder mFingerprintUiHelperBuilder;

    @Inject
    public FingerprintAuthenticationDialogFragment() {
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
        return v;
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

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onPinSuccess(String password) {
        if (mCallback != null) {
            mCallback.onAuthenticated();
        }

        dismiss();
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
    public void showNetworkErrorDialog() {
    }

    @Override
    public void showNetworkErrorDialog(ZPWOnSweetDialogListener listener) {
    }


    @Override
    public void show(FragmentManager manager, String tag) {
        mPresenter.show();
        super.show(manager, tag);
    }

    @Override
    public void onAuthenticated() {
        Timber.d("onAuthenticated");
        if (mCallback != null) {
            mCallback.onAuthenticated();
        }
    }

    @Override
    public void onAuthenticationFailure() {
        Timber.d("onAuthenticationFailure");
        if (mCallback != null) {
            mCallback.onAuthenticationFailure();
        }
    }

    public void setAuthenticationCallback(AuthenticationCallback callback) {
        this.mCallback = callback;
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

    public void setStage(Stage stage) {
        Timber.d("set stage %s presenter %s", stage, mPresenter);
        mPresenter.setStage(stage);
    }
}
