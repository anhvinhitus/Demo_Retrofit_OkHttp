package vn.com.vng.zalopay.account.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 7/1/16.
 */
public abstract class AbsPickerImageFragment extends BaseFragment {


    public static final int CAPTURE_CODE = 200;
    public static final int PICK_IMAGE_CODE = 201;
    public static final int CROPPER_IMAGE_CODE = 202;

    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getContext().getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage, "pickImageResult.jpg"));
        }
        return outputFileUri;
    }

    protected Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    private Uri getCaptureImageOutputUri(String name) {
        Uri outputFileUri = null;
        File getImage = getContext().getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage, name));
        }
        return outputFileUri;
    }

    protected Uri getPickImageResultUri(Intent data, String name) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri(name) : data.getData();
    }

    protected void startPickImage() {
        try {
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            i.setType("image/*");
            startActivityForResult(i, PICK_IMAGE_CODE);
        } catch (Exception ex) {
            Timber.w(ex, "startPickImage");
        }
    }

    protected void startPickImage(int requestCode) {
        try {
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            i.setType("image/*");
            startActivityForResult(i, requestCode);
        } catch (Exception ex) {
            Timber.w(ex, "startPickImage");
        }
    }

    protected void startCaptureImage() {
        try {
            if (checkAndRequestPermission()) {
                Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                File getImage = getContext().getExternalCacheDir();
                i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri
                        .fromFile(new File(getImage, "pickImageResult.jpg")));
                startActivityForResult(i, CAPTURE_CODE);
            }
        } catch (Exception ex) {
            Timber.w(ex, "startCaptureImage");
        }
    }

    protected void startCaptureImage(int requestCode, String name) {
        try {
            if (checkAndRequestPermission()) {
                Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                File getImage = getContext().getExternalCacheDir();
                i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri
                        .fromFile(new File(getImage, name)));
                startActivityForResult(i, requestCode);
            }
        } catch (Exception ex) {
            Timber.w(ex, "startCaptureImage");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_CODE:
                    //  startActivityForResult(ImageCropperActivity.intentInstance(getActivity(), getPickImageResultUri(data)), CROPPER_IMAGE_CODE);
                    break;
                case PICK_IMAGE_CODE:
                    //    startActivityForResult(ImageCropperActivity.intentInstance(getActivity(), getPickImageResultUri(data)), CROPPER_IMAGE_CODE);
                    break;
                case CROPPER_IMAGE_CODE:
                  /*    String path = data.getStringExtra("path");
                        cropImageSuccess(path);
                    Timber.d(" path %s", path);*/

                    break;
                default:
            }
        }
    }


    public boolean checkAndRequestPermission() {
        boolean hasPermission = true;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }
        }
        return hasPermission;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onCapturePermissionAllowed();
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
            }
        }
    }

    private void onCapturePermissionAllowed() {
        startCaptureImage();
    }

    public static class CoverBottomSheetDialogFragment extends BottomSheetDialogFragment {

        public interface OnClickListener {
            void onClickCapture();

            void onClickGallery();
        }


        public static CoverBottomSheetDialogFragment newInstance() {
            Bundle args = new Bundle();
            CoverBottomSheetDialogFragment fragment = new CoverBottomSheetDialogFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        };

        @Override
        public void setupDialog(Dialog dialog, int style) {
            super.setupDialog(dialog, style);
            View contentView = View.inflate(getContext(), R.layout.bottom_sheet_pick_image, null);
            dialog.setContentView(contentView);

            ButterKnife.bind(this, contentView);

            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
            CoordinatorLayout.Behavior behavior = params.getBehavior();

            if (behavior != null && behavior instanceof BottomSheetBehavior) {
                ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @OnClick(R.id.layoutCapture)
        public void onClickCapture(View v) {
            if (listener != null) {
                listener.onClickCapture();
            }
            dismiss();
        }

        @OnClick(R.id.layoutGallery)
        public void onClickGallery(View v) {
            if (listener != null) {
                listener.onClickGallery();
            }
            dismiss();
        }

        @OnClick(R.id.layoutCancel)
        public void onClickCancel(View v) {
            dismiss();
        }

        private OnClickListener listener;

        public void setOnClickListener(OnClickListener listener) {
            this.listener = listener;
        }


        @Override
        public void onDestroyView() {
            this.listener = null;
            super.onDestroyView();
        }
    }


}
