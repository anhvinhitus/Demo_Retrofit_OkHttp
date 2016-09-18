package vn.com.vng.zalopay.account.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.KeyboardLinearLayout;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.UpdateProfile3Presenter;
import vn.com.vng.zalopay.account.ui.view.IUpdateProfile3View;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.ImageLoader;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 6/30/16.
 */
public class UpdateProfile3Fragment extends AbsPickerImageFragment implements IUpdateProfile3View {

    public static UpdateProfile3Fragment newInstance() {

        Bundle args = new Bundle();

        UpdateProfile3Fragment fragment = new UpdateProfile3Fragment();
        fragment.setArguments(args);
        return fragment;
    }


    private static final int BACKGROUND_IMAGE_REQUEST_CODE = 100;
    private static final int FOREGROUND_IMAGE_REQUEST_CODE = 101;
    private static final int AVATAR_REQUEST_CODE = 102;

    @Inject
    UpdateProfile3Presenter presenter;

    @Inject
    User user;

    @BindView(R.id.viewFlipper)
    ViewFlipper viewFlipper;

    @BindView(R.id.textInputEmail)
    TextInputLayout mEmailView;

    @BindView(R.id.textInputIdentity)
    TextInputLayout mIdentityNumberView;

    @BindView(R.id.imgAvatar)
    SimpleDraweeView mAvatarInfoView;

    @BindView(R.id.tvSex)
    TextView tvSex;

    @BindView(R.id.tvBirthday)
    TextView tvBirthday;

    @BindView(R.id.tv_name)
    TextView tvName;

    @BindView(R.id.avatar)
    ImageView mAvatarView;

    @BindView(R.id.ivBgCmnd)
    ImageView mBgCmndView;

    @BindView(R.id.ivFgCmnd)
    ImageView mFgCmndView;

    @BindView(R.id.tvAvatar)
    TextView mTvAvatarView;

    @BindView(R.id.tvBgCmnd)
    TextView mTvBgCmndView;

    @BindView(R.id.tvFgCmnd)
    TextView mTvFgCmndView;

    @BindView(R.id.rootView)
    KeyboardLinearLayout rootView;

    @BindView(R.id.headerView)
    View headerView;

    private Uri mUriBgCmnd;
    private Uri mUriFgCmnd;
    private Uri mUriAvatar;

    @BindView(R.id.tvTerm)
    TextView tvTerm;

    @BindView(R.id.scroll1)
    ScrollView mScrollView;

    @BindView(R.id.container1)
    View mContainerView;

    @BindView(R.id.btnRemoveFrontCmnd)
    ImageView btnRemoveFrontImage;

    @BindView(R.id.btnRemoveBackCmnd)
    ImageView btnRemoveBackImage;

    @BindView(R.id.btnRemoveAvatar)
    ImageView btnRemoveAvatar;

    @Inject
    ImageLoader mImageLoader;

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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        setProfile(user);
        rootView.setOnKeyboardStateListener(new KeyboardLinearLayout.KeyboardHelper.OnKeyboardStateChangeListener() {
            @Override
            public void onKeyBoardShow(int height) {
              /*  int childHeight = mContainerView.getHeight();
                boolean isScrollable = mScrollView.getHeight() < childHeight + mScrollView.getPaddingTop() + mScrollView.getPaddingBottom();

                Timber.d("onKeyBoardShow: childHeight %s isScrollable %s mScrollView %s", childHeight, isScrollable, mScrollView.getHeight());

                headerView.setVisibility(isScrollable ? View.GONE : View.VISIBLE);*/
                headerView.setVisibility(View.GONE);
            }

            @Override
            public void onKeyBoardHide() {
                Timber.d("onKeyBoardHide");
                headerView.setVisibility(View.VISIBLE);
            }
        });

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
    }

    @OnTextChanged(R.id.edtEmail)
    public void onTextChangedEmail(CharSequence s) {
        mEmailView.setError(null);
    }

    @OnTextChanged(R.id.edtIdentity)
    public void onTextChangeIdentity(CharSequence s) {
        mIdentityNumberView.setError(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        presenter.getProfileInfo();
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    public int getCurrentPage() {
        return viewFlipper.getDisplayedChild();
    }

    public void nextPage() {
        viewFlipper.setInAnimation(getContext(), R.anim.in_from_left);
        viewFlipper.setOutAnimation(getContext(), R.anim.out_to_right);
        viewFlipper.showNext();
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

    @OnClick(R.id.btnContinue)
    public void onClickContinue() {
        if (getCurrentPage() == 0) {

            if (isValidatePageOne()) {
                nextPage();
            }

            hideKeyboard();
        } else {
            updateProfile();
        }
    }

    @Override
    public boolean onBackPressed() {
        String email = mEmailView.getEditText().getText().toString();
        String cmnd = mIdentityNumberView.getEditText().getText().toString();

        presenter.saveProfileInfo3(email, cmnd, mUriFgCmnd, mUriBgCmnd, mUriAvatar);

        return super.onBackPressed();
    }

    private void updateProfile() {
        String cmnd = mIdentityNumberView.getEditText().getText().toString();
        String email = mEmailView.getEditText().getText().toString();
        if (isValidatePageTwo()) {
            presenter.updateProfile3(cmnd, email, mUriFgCmnd, mUriBgCmnd, mUriAvatar);
        }
    }

    private boolean isValidatePageOne() {
        if (!ValidateUtil.isEmailAddress(mEmailView.getEditText().getText().toString())) {
            mEmailView.setError(getString(R.string.email_invalid));
            return false;
        }

        if (!ValidateUtil.isCMND(mIdentityNumberView.getEditText().getText().toString())) {
            mIdentityNumberView.setError(getString(R.string.cmnd_invalid));
            return false;
        }

        return true;
    }

    private boolean isValidatePageTwo() {
        if (mUriBgCmnd == null || TextUtils.isEmpty(mUriBgCmnd.getPath())) {
            showToast(R.string.exception_uri_bg_cmnd);
            return false;
        }

        if (mUriFgCmnd == null || TextUtils.isEmpty(mUriFgCmnd.getPath())) {
            showToast(R.string.exception_uri_fg_cmnd);
            return false;
        }

        if (mUriAvatar == null || TextUtils.isEmpty(mUriAvatar.getPath())) {
            showToast(R.string.exception_uri_avatar);
            return false;
        }

        return true;
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
        dialog.setConfirmText(getString(R.string.ok));
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

    @Override
    public void setProfile(User user) {
        tvBirthday.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(user.birthDate * 1000)));
        tvName.setText(user.displayName);
        tvSex.setText(user.getGender());

        mImageLoader.loadImage(mAvatarInfoView, user.avatar);
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
                    mUriBgCmnd = uri;
                    loadBackgroundImageCMND(mUriBgCmnd);
                    break;
                case FOREGROUND_IMAGE_REQUEST_CODE:
                    mUriFgCmnd = uri;
                    loadForegroundImageCMND(mUriFgCmnd);
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
        loadImage(mFgCmndView, uri);
        mTvFgCmndView.setVisibility(View.GONE);
        btnRemoveFrontImage.setClickable(true);
        btnRemoveFrontImage.setImageResource(R.drawable.ic_remove_circle);
    }

    void clearFrontImage() {
        mFgCmndView.setImageDrawable(null);
        mFgCmndView.setVisibility(View.GONE);
        mTvFgCmndView.setVisibility(View.VISIBLE);
        btnRemoveFrontImage.setClickable(false);
        btnRemoveFrontImage.setImageResource(R.drawable.ic_camera);
        mUriFgCmnd = null;
    }

    void loadAvatar(Uri uri) {
        loadImage(mAvatarView, uri);
        mTvAvatarView.setVisibility(View.GONE);
        btnRemoveAvatar.setClickable(true);
        btnRemoveAvatar.setImageResource(R.drawable.ic_remove_circle);
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
        loadImage(mBgCmndView, uri);
        mTvBgCmndView.setVisibility(View.GONE);
        btnRemoveBackImage.setClickable(true);
        btnRemoveBackImage.setImageResource(R.drawable.ic_remove_circle);
    }

    void clearBackgroundImage() {
        mBgCmndView.setImageDrawable(null);
        mBgCmndView.setVisibility(View.GONE);
        mTvBgCmndView.setVisibility(View.VISIBLE);
        btnRemoveBackImage.setClickable(false);
        btnRemoveBackImage.setImageResource(R.drawable.ic_camera);
        mUriBgCmnd = null;
    }

    private void loadImage(ImageView image, Uri uri) {
        if (uri == null) {
            return;
        }
        
        image.setImageURI(uri);
        image.setVisibility(View.VISIBLE);

       /* try {
            Bitmap bitmap = getThumbnail(getContext(), uri);
            image.setImageBitmap(bitmap);
            image.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Timber.d(e, "get thumbnail ");
        }*/
    }

    final int THUMBNAIL_SIZE = 256;

   /* private Bitmap getThumbnail(Context context, Uri uri) throws Exception {
        InputStream input = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }*/

    @Override
    public void setProfileInfo(String email, String identity, String foregroundImg, String backgroundImg, String avatarImg) {
        Timber.d("setProfileInfo: foregroundImg %s backgroundImg %s avatarImg ", foregroundImg, backgroundImg, avatarImg);
        mEmailView.getEditText().setText(email);
        mIdentityNumberView.getEditText().setText(identity);

        if (!TextUtils.isEmpty(foregroundImg)) {
            mUriFgCmnd = Uri.parse(foregroundImg);
            loadForegroundImageCMND(mUriFgCmnd);
        }

        if (!TextUtils.isEmpty(backgroundImg)) {
            mUriBgCmnd = Uri.parse(backgroundImg);
            loadBackgroundImageCMND(mUriBgCmnd);
        }

        if (!TextUtils.isEmpty(avatarImg)) {
            mUriAvatar = Uri.parse(avatarImg);
            loadAvatar(mUriAvatar);
        }
    }
}
