package vn.com.vng.zalopay.account.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zalopay.ui.widget.KeyboardLinearLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.UpdateProfile3Presenter;
import vn.com.vng.zalopay.account.ui.view.IUpdateProfile3View;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.ValidateUtil;

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

    @BindView(R.id.edtEmail)
    EditText mEmailView;

    @BindView(R.id.edtIdentityNumber)
    EditText mIdentityNumberView;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

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
                headerView.setVisibility(View.GONE);
            }

            @Override
            public void onKeyBoardHide() {
                headerView.setVisibility(View.VISIBLE);
            }
        });

        AndroidUtils.setSpannedMessageToView(tvTerm,
                R.string.agree_term_of_use, R.string.term_of_use,
                false, false, R.color.colorPrimary,
                new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        navigator.startTermActivity(getContext());
                    }
                });
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

    private void updateProfile() {
        String cmnd = mIdentityNumberView.getText().toString();
        String email = mEmailView.getText().toString();
        if (isValidatePageTwo()) {
            presenter.updateProfile3(cmnd, email, mUriFgCmnd, mUriBgCmnd, mUriAvatar);
        }
    }

    private boolean isValidatePageOne() {
        if (!ValidateUtil.isEmailAddress(mEmailView.getText().toString())) {
            showToast(R.string.email_invalid);
            return false;
        }

        if (!ValidateUtil.isCMND(mIdentityNumberView.getText().toString())) {
            showToast(R.string.cmnd_invalid);
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
        showToast(R.string.update_profile_success);
        getActivity().finish();
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public void setProfile(User user) {
        tvBirthday.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(user.birthDate * 1000)));
        tvName.setText(user.dname);
        tvSex.setText(user.getGender());

        Glide.with(this).load(user.avatar)
                .placeholder(R.color.silver)
                .error(R.drawable.ic_avatar_default)
                .centerCrop()
                .into(imgAvatar);
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
            return "avatar.jpg";
        } else if (requestCode == BACKGROUND_IMAGE_REQUEST_CODE) {
            return "bgcmnd.jpg";
        } else if (requestCode == FOREGROUND_IMAGE_REQUEST_CODE) {
            return "fgcmnd.jpg";
        } else {
            return "noname.jpg";
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case BACKGROUND_IMAGE_REQUEST_CODE:

                    mUriBgCmnd = getPickImageResultUri(data, getImageNameFromReqCode(requestCode));
                    loadImage(mBgCmndView, mUriBgCmnd);
                    mTvBgCmndView.setVisibility(View.GONE);
                    break;
                case FOREGROUND_IMAGE_REQUEST_CODE:

                    mUriFgCmnd = getPickImageResultUri(data, getImageNameFromReqCode(requestCode));
                    loadImage(mFgCmndView, mUriFgCmnd);
                    mTvFgCmndView.setVisibility(View.GONE);
                    break;
                case AVATAR_REQUEST_CODE:

                    mUriAvatar = getPickImageResultUri(data, getImageNameFromReqCode(requestCode));
                    loadImage(mAvatarView, mUriAvatar);
                    mTvAvatarView.setVisibility(View.GONE);

                    break;
                default:
            }
        }
    }

    private void loadImage(ImageView image, Uri uri) {
        Glide.with(this).loadFromMediaStore(uri)
                .placeholder(R.color.silver)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontTransform()
                .centerCrop()
                .into(image);
        image.setVisibility(View.VISIBLE);
    }
}
