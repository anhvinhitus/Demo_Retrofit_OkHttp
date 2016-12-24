package vn.com.vng.zalopay.account.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.KeyboardLinearLayout;
import com.zalopay.ui.widget.edittext.ZPEditText;
import com.zalopay.ui.widget.layout.OnKeyboardStateChangeListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.UpdateProfile3Presenter;
import vn.com.vng.zalopay.account.ui.view.IUpdateProfile3View;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.validate.EmailValidate;
import vn.com.vng.zalopay.ui.widget.validate.PassportValidate;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 6/30/16.
 * *
 */
public class UpdateProfile3Fragment extends AbsPickerImageFragment implements IUpdateProfile3View,
        OnKeyboardStateChangeListener {

    public static UpdateProfile3Fragment newInstance(boolean focusIdentity) {
        Bundle args = new Bundle();
        args.putBoolean("focusIdentity", focusIdentity);
        UpdateProfile3Fragment fragment = new UpdateProfile3Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final int BACKGROUND_IMAGE_REQUEST_CODE = 100;
    private static final int FOREGROUND_IMAGE_REQUEST_CODE = 101;
    private static final int AVATAR_REQUEST_CODE = 102;

    private static final int PERMISSION_READ_EXTERNAL_STORAGE = 1001;

    @Inject
    UpdateProfile3Presenter presenter;

    @BindView(R.id.rootView)
    KeyboardLinearLayout mRootView;

    @BindView(R.id.scroll1)
    ScrollView mScrollView;

    @BindView(R.id.viewFlipper)
    ViewFlipper mViewFlipper;

    @BindView(R.id.edtEmail)
    ZPEditText mEdtEmailView;

    @BindView(R.id.edtIdentity)
    ZPEditText mEdtIdentityView;

    @BindView(R.id.avatar)
    SimpleDraweeView mAvatarView;

    @BindView(R.id.ivBgCmnd)
    SimpleDraweeView mBgIdentityView;

    @BindView(R.id.ivFgCmnd)
    SimpleDraweeView mFgIdentityView;

    @BindView(R.id.tvAvatar)
    TextView mTvAvatarView;

    @BindView(R.id.tvBgCmnd)
    TextView mTvBgIdentityView;

    @BindView(R.id.tvFgCmnd)
    TextView mTvFgIdentityView;

    private Uri mUriBgIdentity;
    private Uri mUriFgIdentity;
    private Uri mUriAvatar;

    @BindView(R.id.tvTerm)
    TextView tvTerm;

    @BindView(R.id.btnRemoveFrontCmnd)
    ImageView btnRemoveFrontImage;

    @BindView(R.id.btnRemoveBackCmnd)
    ImageView btnRemoveBackImage;

    @BindView(R.id.btnRemoveAvatar)
    ImageView btnRemoveAvatar;

    @BindView(R.id.btnContinue)
    Button mBtnContinue;

    @BindView(R.id.btnSubmit)
    Button mBtnSubmitView;

    boolean focusIdentity;

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_update_profile_3;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        focusIdentity = getArguments().getBoolean("focusIdentity", false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);
        AndroidUtils.setSpannedMessageToView(tvTerm,
                R.string.agree_term_of_use, R.string.term_of_use,
                false, false, R.color.colorPrimary,
                new ClickableSpanNoUnderline() {
                    @Override
                    public void onClick(View widget) {
                        navigator.startTermActivity(getContext());
                    }
                });

        btnRemoveFrontImage.setClickable(false);
        btnRemoveBackImage.setClickable(false);
        btnRemoveAvatar.setClickable(false);
        mBtnContinue.setEnabled(false);
        mBtnSubmitView.setEnabled(false);

        mViewFlipper.setFlipInterval(0);
        mViewFlipper.setInAnimation(null);
        mViewFlipper.setOutAnimation(null);

        focusInputText(focusIdentity ? mEdtIdentityView : mEdtEmailView);

        mEdtEmailView.addValidator(new EmailValidate(getString(R.string.email_invalid)));
        mEdtIdentityView.addValidator(new PassportValidate(getString(R.string.cmnd_passport_invalid)));

        mRootView.setOnKeyboardStateListener(this);
    }

    private void focusInputText(EditText input) {
        input.requestFocus();
        setSelection(input);
    }

    private void setSelection(EditText input) {
        if (input != null && input.length() > 0) {
            if (getActivity().getCurrentFocus() == input) {
                input.setSelection(input.length());
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.getProfileInfo();
    }

    @Override
    public void onDestroyView() {
        mEdtIdentityView.clearValidators();
        mEdtEmailView.clearValidators();
        presenter.detachView();
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed() {

        if (getCurrentPage() > 0) {
            previousPage();
            return true;
        }

        String email = getEmail();
        String identity = getIdentity();

        presenter.saveProfileInfo3(email, identity, mUriFgIdentity, mUriBgIdentity, mUriAvatar);

        return super.onBackPressed();
    }

    @OnTextChanged(value = R.id.edtEmail, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTextChangedEmail(CharSequence s) {
        mBtnContinue.setEnabled(mEdtEmailView.isValid() && mEdtIdentityView.isValid());
    }

    @OnTextChanged(value = R.id.edtIdentity, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTextChangeIdentity(CharSequence s) {
        mBtnContinue.setEnabled(mEdtEmailView.isValid() && mEdtIdentityView.isValid());
    }

    @OnClick(R.id.btnRemoveAvatar)
    public void onClickRemoveAvatar() {
        loadAvatar(null);
    }

    @OnClick(R.id.btnRemoveBackCmnd)
    public void onClickRemoveBackImage() {
        loadBackgroundImage(null);
    }

    @OnClick(R.id.btnRemoveFrontCmnd)
    public void onClickRemoveFrontImage() {
        loadFrontImage(null);
    }

    @OnClick(R.id.btnSubmit)
    public void onClickConfirm() {
        updateProfile();
    }

    @OnClick(R.id.layoutFgCmnd)
    public void onClickFgIdentity() {
        showBottomSheetDialog(FOREGROUND_IMAGE_REQUEST_CODE);
    }

    @OnClick(R.id.layoutBgCmnd)
    public void onClickBgIdentity() {
        showBottomSheetDialog(BACKGROUND_IMAGE_REQUEST_CODE);
    }

    @OnClick(R.id.layoutAvatar)
    public void onClickAvatar() {
        showBottomSheetDialog(AVATAR_REQUEST_CODE);
    }

    public int getCurrentPage() {
        return mViewFlipper.getDisplayedChild();
    }

    private void nextPage() {
        mViewFlipper.showNext();
    }

    private void previousPage() {
        mViewFlipper.showPrevious();
    }

    private String getEmail() {
        if (mEdtEmailView != null) {
            return mEdtEmailView.getText().toString();
        }
        return "";
    }

    private String getIdentity() {
        if (mEdtIdentityView != null) {
            return mEdtIdentityView.getText().toString().toUpperCase();
        }

        return "";
    }

    private void setEmail(String text) {
        if (mEdtEmailView != null) {
            mEdtEmailView.setText(text);
        }
    }

    private void setIdentity(String text) {
        if (mEdtIdentityView != null) {
            mEdtIdentityView.setText(text);
        }
    }

    private void checkIfNoInput() {

        if (mUriFgIdentity == null || mUriBgIdentity == null || mUriAvatar == null) {
            mBtnSubmitView.setEnabled(false);
            return;
        }

        if (mAvatarView.getDrawable() == null || mFgIdentityView.getDrawable() == null || mBgIdentityView.getDrawable() == null) {
            mBtnSubmitView.setEnabled(false);
            return;
        }

        mBtnSubmitView.setEnabled(true);
    }

    private void updateProfile() {
        String identity = getIdentity();
        String email = getEmail();
        presenter.updateProfile3(identity, email, mUriFgIdentity, mUriBgIdentity, mUriAvatar);
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
    public void updateSuccess() {
        showDialogSuccess();
    }

    private void showDialogSuccess() {
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.INFO_TYPE, R.style.alert_dialog);
        dialog.setContentText(getString(R.string.update_profile_success));
        dialog.setConfirmText(getString(R.string.txt_close));
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public void waitingApproveProfileLevel3() {
        hideLoading();
        getActivity().finish();
    }

    private void showBottomSheetDialog(final int requestCode) {
        CoverBottomSheetDialogFragment dialog = CoverBottomSheetDialogFragment.newInstance();
        dialog.setOnClickListener(new CoverBottomSheetDialogFragment.OnClickListener() {
            @Override
            public void onClickCapture() {
                startCaptureImage(requestCode, getImageNameFromReqCode(requestCode));
            }

            @Override
            public void onClickGallery() {
                startPickImage(requestCode);
            }
        });
        dialog.show(getChildFragmentManager(), "bottomsheet");
    }

    private String getImageNameFromReqCode(int requestCode) {
        if (requestCode == AVATAR_REQUEST_CODE) {
            return "avatar";
        } else if (requestCode == BACKGROUND_IMAGE_REQUEST_CODE) {
            return "bgcmnd";
        } else if (requestCode == FOREGROUND_IMAGE_REQUEST_CODE) {
            return "fgcmnd";
        } else {
            return "noname";
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Timber.d("onActivityResult: requestCode %s resultCode %s", requestCode, resultCode);

        if (resultCode == Activity.RESULT_OK) {
            Uri uri = getPickImageResultUri(data, getImageNameFromReqCode(requestCode));
            if (uri == null) {
                return;
            }

            Timber.d("onActivityResult: uri %s", uri.toString());

            switch (requestCode) {
                case BACKGROUND_IMAGE_REQUEST_CODE:
                    mUriBgIdentity = uri;
                    loadBackgroundImage(mUriBgIdentity);
                    break;
                case FOREGROUND_IMAGE_REQUEST_CODE:
                    mUriFgIdentity = uri;
                    loadFrontImage(mUriFgIdentity);
                    break;
                case AVATAR_REQUEST_CODE:
                    mUriAvatar = uri;
                    loadAvatar(mUriAvatar);
                    break;
                default:
            }
        }
    }

    private void loadFrontImage(@Nullable Uri uri) {
        Timber.d("loadFrontImage uri[%s]", uri);

        if (uri == null) {
            clearFrontImage();
            return;
        }

        mFgIdentityView.setImageURI(uri);
        mFgIdentityView.setVisibility(View.VISIBLE);

        mTvFgIdentityView.setVisibility(View.GONE);
        btnRemoveFrontImage.setClickable(true);
        btnRemoveFrontImage.setImageResource(R.drawable.ic_remove_circle);

        checkIfNoInput();
    }

    private void clearFrontImage() {
        mFgIdentityView.setImageDrawable(null);
        mFgIdentityView.setVisibility(View.GONE);
        mTvFgIdentityView.setVisibility(View.VISIBLE);
        btnRemoveFrontImage.setClickable(false);
        btnRemoveFrontImage.setImageResource(R.drawable.ic_camera);
        mUriFgIdentity = null;

        checkIfNoInput();
    }

    private void loadAvatar(@Nullable Uri uri) {
        if (uri == null) {
            clearAvatar();
            return;
        }
        Timber.d("loadAvatar uri[%s]", uri);
        mAvatarView.setImageURI(uri);
        mAvatarView.setVisibility(View.VISIBLE);

        mTvAvatarView.setVisibility(View.GONE);
        btnRemoveAvatar.setClickable(true);
        btnRemoveAvatar.setImageResource(R.drawable.ic_remove_circle);

        checkIfNoInput();
    }

    private void clearAvatar() {
        mAvatarView.setImageDrawable(null);
        mAvatarView.setVisibility(View.GONE);
        mTvAvatarView.setVisibility(View.VISIBLE);

        btnRemoveAvatar.setClickable(false);
        btnRemoveAvatar.setImageResource(R.drawable.ic_camera);
        mUriAvatar = null;

        checkIfNoInput();
    }

    private void loadBackgroundImage(@Nullable Uri uri) {
        Timber.d("load background image [%s]", uri);
        if (uri == null) {
            clearBackgroundImage();
            return;
        }

        mBgIdentityView.setImageURI(uri);
        mBgIdentityView.setVisibility(View.VISIBLE);

        mTvBgIdentityView.setVisibility(View.GONE);
        btnRemoveBackImage.setClickable(true);
        btnRemoveBackImage.setImageResource(R.drawable.ic_remove_circle);

        checkIfNoInput();
    }

    private void clearBackgroundImage() {
        mBgIdentityView.setImageDrawable(null);
        mBgIdentityView.setVisibility(View.GONE);
        mTvBgIdentityView.setVisibility(View.VISIBLE);
        btnRemoveBackImage.setClickable(false);
        btnRemoveBackImage.setImageResource(R.drawable.ic_camera);
        mUriBgIdentity = null;

        checkIfNoInput();
    }

    @Override
    public void setProfileInfo(String email, String identity, String foregroundImg, String backgroundImg, String avatarImg) {
        Timber.d("setProfileInfo: foregroundImg %s backgroundImg %s avatarImg ", foregroundImg, backgroundImg, avatarImg);

        if (!TextUtils.isEmpty(email)) {
            setEmail(email);
        }
        if (!TextUtils.isEmpty(identity)) {
            setIdentity(identity);
        }

        setSelection(mEdtEmailView);
        setSelection(mEdtIdentityView);

        mBtnContinue.setEnabled(ValidateUtil.isEmailAddress(getEmail()) && ValidateUtil.isValidCMNDOrPassport(getIdentity()));

        boolean shouldRequestPermission = false;

        if (!TextUtils.isEmpty(foregroundImg)) {
            mUriFgIdentity = Uri.parse(foregroundImg);
            shouldRequestPermission = true;
        }
        if (!TextUtils.isEmpty(backgroundImg)) {
            mUriBgIdentity = Uri.parse(backgroundImg);
            shouldRequestPermission = true;
        }
        if (!TextUtils.isEmpty(avatarImg)) {
            mUriAvatar = Uri.parse(avatarImg);
            shouldRequestPermission = true;
        }

        if (!shouldRequestPermission) {
            return;
        }

        if (!isPermissionGrantedAndRequest(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_READ_EXTERNAL_STORAGE)) {
            return;
        }

        loadFrontImage(mUriFgIdentity);
        loadBackgroundImage(mUriBgIdentity);
        loadAvatar(mUriAvatar);

    }

    @OnFocusChange({R.id.edtEmail, R.id.edtIdentity})
    public void onFocusChange(View v, boolean hasView) {
        Timber.d("onFocusChange %s", hasView);
        mBtnContinue.setEnabled(mEdtEmailView.isValid() && mEdtIdentityView.isValid());
    }

    @OnClick(R.id.btnContinue)
    public void onClickContinue() {

        if (!mEdtEmailView.validate() || !mEdtIdentityView.validate()) {
            return;
        }

        nextPage();
        hideKeyboard();
    }

    @Override
    public void onKeyBoardShow(int height) {

        if (mEdtEmailView.isFocused()) {
            int[] location = new int[2];
            Timber.d("onKeyBoardShow scroll to Top");
            mEdtIdentityView.getLocationInWindow(location);
            Timber.d("onKeyBoardShow: mEdtIdentityView.y %s", location[1]);
            mScrollView.smoothScrollBy(0, location[1]);
        } else if (mEdtIdentityView.isFocused()) {
            mScrollView.smoothScrollBy(0, mScrollView.getBottom());
        }
    }

    @Override
    public void onKeyBoardHide() {

    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {
        super.permissionGranted(permissionRequestCode, isGranted);

        if (!isGranted) {
            return;
        }

        switch (permissionRequestCode) {
            case PERMISSION_READ_EXTERNAL_STORAGE:

                loadFrontImage(mUriFgIdentity);
                loadBackgroundImage(mUriBgIdentity);
                loadAvatar(mUriAvatar);

                break;
        }
    }
}
