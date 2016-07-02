package vn.com.vng.zalopay.account.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.UpdateProfile3Presenter;
import vn.com.vng.zalopay.account.ui.view.IUpdateProfile3View;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.UriUtil;
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


    public static final int BACKGROUND_IMAGE_REQUEST_CODE = 100;
    public static final int FOREGROUND_IMAGE_REQUEST_CODE = 101;
    public static final int AVATAR_REQUEST_CODE = 102;

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

    public void previousPage() {
        viewFlipper.setInAnimation(getContext(), R.anim.in_from_right);
        viewFlipper.setOutAnimation(getContext(), R.anim.out_to_left);
        viewFlipper.showPrevious();
    }


    private Uri mUriBgCmnd;
    private Uri mUriFgCmnd;
    private Uri mUriAvatar;

    @OnClick(R.id.btnContinue)
    public void onClickContinue(View v) {
        if (getCurrentPage() == 0) {
            if (isValidatePageOne()) {
                nextPage();
            }
        } else {
            String cmnd = mIdentityNumberView.getText().toString();
            String email = mEmailView.getText().toString();
            if (isValidatePageTwo()) {
                presenter.update(cmnd, email, UriUtil.getPath(getContext(), mUriFgCmnd), UriUtil.getPath(getContext(), mUriBgCmnd), UriUtil.getPath(getContext(), mUriAvatar));
            }
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
        showProgressDialog();
    }

    @Override
    public void hideLoading() {
        hideProgressDialog();
    }

    @Override
    public void updateSuccess() {
        getActivity().finish();
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    public void setProfile(User user) {
        tvBirthday.setText(new SimpleDateFormat("dd/MM/yyyy")
                .format(new Date(user.birthDate * 1000)));
        tvName.setText(user.dname);
        tvSex.setText(user.getGender());

        Glide.with(this).load(user.avatar)
                .placeholder(R.color.silver)
                .centerCrop()
                .into(imgAvatar);
    }

    @OnClick(R.id.layoutFgCmnd)
    public void onClickFgCmnd(View v) {
        CoverBottomSheetDialogFragment dialog = CoverBottomSheetDialogFragment.newInstance();
        dialog.setOnClickListener(new CoverBottomSheetDialogFragment.OnClickListener() {
            @Override
            public void onClickCapture() {
                startCaptureImage(FOREGROUND_IMAGE_REQUEST_CODE, "fgcmnd.jpg");
            }

            @Override
            public void onClickGallery() {
                startPickImage(FOREGROUND_IMAGE_REQUEST_CODE);
            }
        });
        dialog.show(getChildFragmentManager(), "FgCmnd");
    }

    @OnClick(R.id.layoutBgCmnd)
    public void onClickBgCmnd(View v) {
        CoverBottomSheetDialogFragment dialog = CoverBottomSheetDialogFragment.newInstance();
        dialog.setOnClickListener(new CoverBottomSheetDialogFragment.OnClickListener() {
            @Override
            public void onClickCapture() {
                startCaptureImage(BACKGROUND_IMAGE_REQUEST_CODE, "bgcmnd.jpg");
            }

            @Override
            public void onClickGallery() {
                startPickImage(BACKGROUND_IMAGE_REQUEST_CODE);
            }
        });
        dialog.show(getChildFragmentManager(), "BgCmnd");
    }

    @OnClick(R.id.layoutAvatar)
    public void onClickAvatar(View v) {
        CoverBottomSheetDialogFragment dialog = CoverBottomSheetDialogFragment.newInstance();
        dialog.setOnClickListener(new CoverBottomSheetDialogFragment.OnClickListener() {
            @Override
            public void onClickCapture() {
                startCaptureImage(AVATAR_REQUEST_CODE, "avatar.jpg");
            }

            @Override
            public void onClickGallery() {
                startPickImage(AVATAR_REQUEST_CODE);
            }
        });
        dialog.show(getChildFragmentManager(), "Avatar");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case BACKGROUND_IMAGE_REQUEST_CODE:
                    //  startActivityForResult(ImageCropperActivity.intentInstance(getActivity(), getPickImageResultUri(data)), CROPPER_IMAGE_CODE);
                    mUriBgCmnd = getPickImageResultUri(data, "bgcmnd.jpg");
                    setImage(mBgCmndView, mUriBgCmnd);
                    mTvBgCmndView.setVisibility(View.GONE);
                    break;
                case FOREGROUND_IMAGE_REQUEST_CODE:
                    //    startActivityForResult(ImageCropperActivity.intentInstance(getActivity(), getPickImageResultUri(data)), CROPPER_IMAGE_CODE);
                    mUriFgCmnd = getPickImageResultUri(data, "fgcmnd.jpg");
                    setImage(mFgCmndView, mUriFgCmnd);
                    mTvFgCmndView.setVisibility(View.GONE);
                    break;
                case AVATAR_REQUEST_CODE:

                    mUriAvatar = getPickImageResultUri(data, "avatar.jpg");
                    setImage(mAvatarView, mUriAvatar);
                    mTvAvatarView.setVisibility(View.GONE);

                    break;
                default:
            }
        }
    }

    private void setImage(ImageView image, Uri uri) {
        Glide.with(this).load(uri)
                .placeholder(R.color.silver)
                .centerCrop()
                .into(image);
        image.setVisibility(View.VISIBLE);
    }


}
