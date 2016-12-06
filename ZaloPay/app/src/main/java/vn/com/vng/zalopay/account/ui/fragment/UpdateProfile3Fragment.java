package vn.com.vng.zalopay.account.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.UpdateProfile3Presenter;
import vn.com.vng.zalopay.account.ui.view.IUpdateProfile3View;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.validate.EmailValidate;
import vn.com.vng.zalopay.ui.widget.validate.PassportValidate;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.PhotoUtil;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
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

    @Inject
    UpdateProfile3Presenter presenter;

    @BindView(R.id.rootView)
    KeyboardLinearLayout mRootView;

    @BindView(R.id.scroll1)
    ScrollView mScrollView;

    @BindView(R.id.viewFlipper)
    ViewFlipper viewFlipper;

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
        presenter.setView(this);
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

        //Request permission for read photo from storage by cache.
        isPermissionReadStorageAndRequest();
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed() {
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
        clearAvatar();
    }

    @OnClick(R.id.btnRemoveBackCmnd)
    public void onClickRemoveBackImage() {
        clearBackgroundImage();
    }

    @OnClick(R.id.btnRemoveFrontCmnd)
    public void onClickRemoveFrontImage() {
        clearFrontImage();
    }

    @OnClick(R.id.btnConfirm)
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
        return viewFlipper.getDisplayedChild();
    }

    public void nextPage() {
        viewFlipper.setInAnimation(getContext(), R.anim.in_from_left);
        viewFlipper.setOutAnimation(getContext(), R.anim.out_to_right);
        viewFlipper.showNext();
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


    private void updateProfile() {
        String identity = getIdentity();
        String email = getEmail();

        if (isValidatePageTwo()) {
            presenter.updateProfile3(identity, email, mUriFgIdentity, mUriBgIdentity, mUriAvatar);
        }
    }

    private boolean isValidatePageTwo() {
        if (mUriFgIdentity == null || TextUtils.isEmpty(mUriFgIdentity.getPath())) {
            showMessageDialog(R.string.exception_uri_fg_cmnd);
            return false;
        }

        if (mUriBgIdentity == null || TextUtils.isEmpty(mUriBgIdentity.getPath())) {
            showMessageDialog(R.string.exception_uri_bg_cmnd);
            return false;
        }

        if (mUriAvatar == null || TextUtils.isEmpty(mUriAvatar.getPath())) {
            showMessageDialog(R.string.exception_uri_avatar);
            return false;
        }

        return true;
    }

    private void showMessageDialog(int message) {
        DialogManager.showSweetDialogCustom(getActivity(), getString(message),
                getString(R.string.txt_close), SweetAlertDialog.NORMAL_TYPE, null);
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
                    loadBackgroundImageCMND(mUriBgIdentity);
                    break;
                case FOREGROUND_IMAGE_REQUEST_CODE:
                    mUriFgIdentity = uri;
                    loadForegroundImageCMND(mUriFgIdentity);
                    break;
                case AVATAR_REQUEST_CODE:
                    mUriAvatar = uri;
                    loadAvatar(mUriAvatar);
                    break;
                default:
            }
        }
    }

    void loadForegroundImageCMND(Uri uri) {
        Timber.d("loadForegroundImageCMND uri[%s]", uri);
        loadImage(uri, new DefaultSubscriber<Bitmap>() {
            @Override
            public void onNext(Bitmap bitmap) {
                Timber.d("loadForegroundImageCMND bitmap[%s]", bitmap);
                if (bitmap == null) {
                    return;
                }
                mFgIdentityView.setImageBitmap(bitmap);
                mFgIdentityView.setVisibility(View.VISIBLE);

                mTvFgIdentityView.setVisibility(View.GONE);
                btnRemoveFrontImage.setClickable(true);
                btnRemoveFrontImage.setImageResource(R.drawable.ic_remove_circle);
            }

            @Override
            public void onError(Throwable e) {
                Timber.w("loadForegroundImageCMND onError[%s]", e.getMessage());
                super.onError(e);
            }

            @Override
            public void onCompleted() {
                Timber.w("loadForegroundImageCMND onCompleted");
                super.onCompleted();
            }
        });
    }

    void clearFrontImage() {
        mFgIdentityView.setImageDrawable(null);
        mFgIdentityView.setVisibility(View.GONE);
        mTvFgIdentityView.setVisibility(View.VISIBLE);
        btnRemoveFrontImage.setClickable(false);
        btnRemoveFrontImage.setImageResource(R.drawable.ic_camera);
        mUriFgIdentity = null;
    }

    void loadAvatar(Uri uri) {
        Timber.d("loadAvatar uri[%s]", uri);
        loadImage(uri, new DefaultSubscriber<Bitmap>() {
            @Override
            public void onNext(Bitmap bitmap) {
                Timber.d("loadAvatar bitmap[%s]", bitmap);
                if (bitmap == null) {
                    return;
                }
                mAvatarView.setImageBitmap(bitmap);
                mAvatarView.setVisibility(View.VISIBLE);

                mTvAvatarView.setVisibility(View.GONE);
                btnRemoveAvatar.setClickable(true);
                btnRemoveAvatar.setImageResource(R.drawable.ic_remove_circle);
            }
        });
    }

    void clearAvatar() {
        mAvatarView.setImageDrawable(null);
        mAvatarView.setVisibility(View.GONE);
        mTvAvatarView.setVisibility(View.VISIBLE);

        btnRemoveAvatar.setClickable(false);
        btnRemoveAvatar.setImageResource(R.drawable.ic_camera);
        mUriAvatar = null;
    }

    void loadBackgroundImageCMND(Uri uri) {
        Timber.d("loadBackgroundImageCMND uri[%s]", uri);
        loadImage(uri, new DefaultSubscriber<Bitmap>() {
            @Override
            public void onNext(Bitmap bitmap) {
                Timber.d("loadBackgroundImageCMND bitmap[%s]", bitmap);
                if (bitmap == null) {
                    return;
                }
                mBgIdentityView.setImageBitmap(bitmap);
                mBgIdentityView.setVisibility(View.VISIBLE);

                mTvBgIdentityView.setVisibility(View.GONE);
                btnRemoveBackImage.setClickable(true);
                btnRemoveBackImage.setImageResource(R.drawable.ic_remove_circle);
            }
        });
    }

    void clearBackgroundImage() {
        mBgIdentityView.setImageDrawable(null);
        mBgIdentityView.setVisibility(View.GONE);
        mTvBgIdentityView.setVisibility(View.VISIBLE);
        btnRemoveBackImage.setClickable(false);
        btnRemoveBackImage.setImageResource(R.drawable.ic_camera);
        mUriBgIdentity = null;
    }

    private void loadImage(final Uri uri, DefaultSubscriber<Bitmap> subscriber) {
        Timber.d("loadImage uri[%s] subscriber[%s]", uri, subscriber);
        if (subscriber == null) {
            return;
        }

        if (uri == null) {
            subscriber.onNext(null);
        }

        ObservableHelper.makeObservable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                try {
                    return PhotoUtil.getThumbnail(getContext(), uri);
                } catch (FileNotFoundException e) {
                    showToast(R.string.exception_file_not_found);
                    Timber.w(e, "get thumbnail ");
                } catch (IOException e) {
                    Timber.w(e, "loadImage");
                }
                return null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
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

        if (!TextUtils.isEmpty(foregroundImg)) {
            mUriFgIdentity = Uri.parse(foregroundImg);
            loadForegroundImageCMND(mUriFgIdentity);
        }

        if (!TextUtils.isEmpty(backgroundImg)) {
            mUriBgIdentity = Uri.parse(backgroundImg);
            loadBackgroundImageCMND(mUriBgIdentity);
        }

        if (!TextUtils.isEmpty(avatarImg)) {
            mUriAvatar = Uri.parse(avatarImg);
            loadAvatar(mUriAvatar);
        }
    }

    @Override
    protected void permissionGranted(int permissionRequestCode) {
        if (permissionRequestCode == Constants.Permission.REQUEST_READ_STORAGE) {
            if (mAvatarView == null || mFgIdentityView == null || mBgIdentityView == null) {
                return;
            }
            if (mAvatarView.getDrawable() == null && mFgIdentityView.getDrawable() == null
                    && mBgIdentityView.getDrawable() == null) {
                presenter.getProfileInfo();
            }
        }
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
        Timber.d("onKeyBoardShow: mEdtEmailView.isFocused() %s", mEdtEmailView.isFocused());
        Timber.d("onKeyBoardShow: mEdtIdentityView.isFocused() %s", mEdtIdentityView.isFocused());
        int[] location = new int[2];
        if (mEdtEmailView.isFocused()) {
            Timber.d("onKeyBoardShow scroll to Top");
            mEdtIdentityView.getLocationInWindow(location);
            Timber.d("onKeyBoardShow: mEdtIdentityView.y %s", location[1]);
            mScrollView.smoothScrollBy(0, location[1]);
        } else if (mEdtIdentityView.isFocused()) {
//            mEdtIdentityView.getLocationInWindow(location);
//            Timber.d("onKeyBoardShow: edtPhone.y %s", location[1]);
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    @Override
    public void onKeyBoardHide() {

    }
}
