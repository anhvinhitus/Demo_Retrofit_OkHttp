package vn.com.vng.zalopay.account.ui.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.FileProvider;
import android.view.View;

import com.zalopay.apploader.internal.FileUtils;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.utils.PhotoUtil;

/**
 * Created by AnhHieu on 7/1/16.
 * *
 */
public abstract class AbsPickerImageFragment extends RuntimePermissionFragment {

    private int mPickImageRequestCode;
    private int mCaptureImageRequestCode;
    private String mImageName;

    private Uri getCaptureImageOutputUri(String name, boolean removeOldFile) {
        Uri outputFileUri = null;
        File photoFile = PhotoUtil.createPhotoFile(getContext(), name);
        if (removeOldFile) {
            if (photoFile.exists()) {
                photoFile.delete();
            } else {
                photoFile.getParentFile().mkdirs();
            }
        }

        Timber.d("Get capture image output : Uri %s", photoFile);
        if (photoFile != null) {
            outputFileUri = FileProvider.getUriForFile(getContext(), getString(R.string.file_provider), photoFile);
        }
        return outputFileUri;
    }

    protected Uri getPickImageResultUri(Intent data, String name) {
        Timber.d("Get pick image result uri : data %s", data);
        if (data != null && data.getData() != null) {
            return data.getData();
        }
        return getCaptureImageOutputUri(name, false);
    }

    protected void startPickImage(int requestCode) {
        mPickImageRequestCode = requestCode;
        if (!isPermissionGrantedAndRequest(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE.READ_EXTERNAL_STORAGE)) {
            return;
        }

        Intent i = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        i.setType("image/*");

        if (i.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(i, requestCode);
        } else {
            Timber.d("No Activity found to handle Intent %s" + Intent.ACTION_PICK);
            showToast(R.string.missing_image_pick);
        }
    }

    protected void startCaptureImage(int requestCode, String name) {
        mCaptureImageRequestCode = requestCode;
        mImageName = name;

        if (!isPermissionGrantedAndRequest(new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE.CAMERA)) {
            return;
        }


        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri contentUri = getCaptureImageOutputUri(name, true);
        Timber.d("Start capture image : uri [%s]", contentUri.toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            List<ResolveInfo> resInfoList =
                    getContext().getPackageManager()
                            .queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                getContext().grantUriPermission(packageName, contentUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        i.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

        if (i.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(i, requestCode);
        } else {
            Timber.d("No Activity found to handle Intent %s" + MediaStore.ACTION_IMAGE_CAPTURE);
            showToast(R.string.missing_image_capture);
        }

    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {
        if (!isGranted) {
            mImageName = null;
            mPickImageRequestCode = 0;
            mCaptureImageRequestCode = 0;
            return;
        }

        switch (permissionRequestCode) {
            case PERMISSION_CODE.READ_EXTERNAL_STORAGE:
                onPickImagePermissionAllowed();
                break;
            case PERMISSION_CODE.CAMERA:
                onCapturePermissionAllowed();
                break;
        }
    }

    private void onCapturePermissionAllowed() {
        if (mCaptureImageRequestCode > 0) {
            startCaptureImage(mCaptureImageRequestCode, mImageName);
        }
    }

    private void onPickImagePermissionAllowed() {
        if (mPickImageRequestCode > 0) {
            startPickImage(mPickImageRequestCode);
        }
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
        public void onClickCapture() {
            if (listener != null) {
                listener.onClickCapture();
            }
            dismiss();
        }

        @OnClick(R.id.layoutGallery)
        public void onClickGallery() {
            if (listener != null) {
                listener.onClickGallery();
            }
            dismiss();
        }

        @OnClick(R.id.layoutCancel)
        public void onClickCancel() {
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
