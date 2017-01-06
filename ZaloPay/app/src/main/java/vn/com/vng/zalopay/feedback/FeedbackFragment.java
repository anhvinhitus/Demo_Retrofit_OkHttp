package vn.com.vng.zalopay.feedback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.zalopay.ui.widget.edittext.ZPEditText;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.AbsPickerImageFragment;
import vn.com.vng.zalopay.data.UserCollector;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.widget.validate.EmailValidate;
import vn.zalopay.feedback.FeedbackCollector;
import vn.zalopay.feedback.collectors.AppCollector;
import vn.zalopay.feedback.collectors.DeviceCollector;
import vn.zalopay.feedback.collectors.NetworkCollector;

public class FeedbackFragment extends AbsPickerImageFragment implements
        FeedbackAdapter.OnClickAddListener, FeedbackAdapter.OnClickDeleteListener, IFeedbackView {

    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_send_feedback;
    }

    private static final int IMAGE_REQUEST_CODE = 100;

    private FeedbackAdapter mAdapter;
    private Uri mUri;
    private FeedbackCollector mCollector;
    private UserCollector mUserCollector;
    private AppCollector mAppCollector;
    private DeviceCollector mDeviceCollector;

    @BindView(R.id.tvTransactionType)
    TextView mCategoryView;
    @BindView(R.id.edtTransactionId)
    ZPEditText mEdtTransactionId;
    @BindView(R.id.edtEmail)
    ZPEditText mEdtEmail;
    @BindView(R.id.edtDescribe)
    ZPEditText mEdtDescribe;

    @BindView(R.id.txtTitleImage)
    TextView mTvTitleImage;
    @BindView(R.id.listView)
    RecyclerView mRecyclerView;

    @BindView(R.id.btnSend)
    View mBtnSendView;

    @Inject
    FeedbackPresenter mPresenter;

    @Inject
    User mUser;

    private String mCategory;
    private String mTransactionId;

    //compress format png
    @Nullable
    private byte[] mScreenshot;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAdapter = new FeedbackAdapter(getContext(), this, this);

        mCollector = new FeedbackCollector();
        mUserCollector = new UserCollector(getAppComponent().userConfig());
        mAppCollector = new AppCollector(AndroidApplication.instance());
        mDeviceCollector = new DeviceCollector();

        initArgs(getActivity().getIntent().getExtras());
    }

    private void initArgs(Bundle bundle) {
        if (bundle == null) {
            return;
        }

        mCategory = bundle.getString("category");
        mTransactionId = bundle.getString("transactionid");
        mScreenshot = bundle.getByteArray("screenshot");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(mAdapter);

        mEdtEmail.addValidator(new EmailValidate(getString(R.string.email_invalid)));

        setEmail(mUser.email);
        mCategoryView.setText(mCategory);
        mEdtTransactionId.setText(mTransactionId);

        if (mScreenshot != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(mScreenshot, 0, mScreenshot.length);
            if (bitmap != null) {
                insertScreenshot(new ScreenshotData(bitmap));
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        collectInformation();
    }

    private void insertScreenshot(ScreenshotData data) {
        mAdapter.insert(data);
        setImageCount();
    }

    public void removeScreenshot(int position) {
        mAdapter.remove(position);
        setImageCount();
    }

    @Override
    public void onDestroyView() {
        mRecyclerView.setAdapter(null);
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @OnTextChanged(value = R.id.edtDescribe, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onTextChangedDescribe() {
        mEdtDescribe.setFloatingLabelText(Html.fromHtml(
                String.format(getResources().getString(R.string.describe_error_count),
                        String.valueOf(mEdtDescribe.getText().length()))));
    }

    @OnTextChanged(value = R.id.edtDescribe, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChangedDescribe() {
        mBtnSendView.setEnabled(mEdtEmail.isValid() && mEdtDescribe.isValid());
    }

    @OnTextChanged(value = R.id.edtEmail, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChangedEmail() {
        mBtnSendView.setEnabled(mEdtEmail.isValid() && mEdtDescribe.isValid());
    }

    @OnFocusChange({R.id.edtEmail, R.id.edtDescribe})
    public void onFocusChange(View v, boolean hasView) {
        Timber.d("onFocusChange %s", hasView);
        mBtnSendView.setEnabled(mEdtEmail.isValid() && mEdtDescribe.isValid());
    }

    @OnClick(R.id.btnSend)
    public void onClickSend() {
        if (!mEdtEmail.validate() || !mEdtDescribe.validate()) {
            return;
        }

        mCollector.startCollectors();
    }

    @OnCheckedChanged(value = {R.id.swSendUserInfor, R.id.swSendDeviceInfor, R.id.swSendAppInfor})
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int itemId = compoundButton.getId();
        switch (itemId) {
            case R.id.swSendUserInfor:

                if (b) {
                    mCollector.installCollector(mUserCollector);
                } else {
                    mCollector.removeCollector(mUserCollector);
                }

                break;
            case R.id.swSendDeviceInfor:

                if (b) {
                    mCollector.installCollector(mDeviceCollector);
                } else {
                    mCollector.removeCollector(mDeviceCollector);
                }

                break;
            case R.id.swSendAppInfor:

                if (b) {
                    mCollector.installCollector(mAppCollector);
                } else {
                    mCollector.removeCollector(mAppCollector);
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onClickAdd() {
        showBottomSheetDialog(IMAGE_REQUEST_CODE);
    }

    @Override
    public void onClickDelete(int position) {
        removeScreenshot(position);
    }

    private void showBottomSheetDialog(final int requestCode) {
        AbsPickerImageFragment.CoverBottomSheetDialogFragment dialog = AbsPickerImageFragment.CoverBottomSheetDialogFragment.newInstance();
        dialog.setOnClickListener(new AbsPickerImageFragment.CoverBottomSheetDialogFragment.OnClickListener() {
            @Override
            public void onClickCapture() {
                startCaptureImage(requestCode, "image");
            }

            @Override
            public void onClickGallery() {
                startPickImage(requestCode);
            }
        });
        dialog.show(getChildFragmentManager(), "bottomsheet");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Timber.d("onActivityResult: requestCode %s resultCode %s", requestCode, resultCode);

        if (resultCode == Activity.RESULT_OK) {
            Uri uri = getPickImageResultUri(data, "image");
            if (uri == null) {
                return;
            }

            Timber.d("onActivityResult: uri %s", uri.toString());

            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    try {
                        mUri = uri;
                        loadScreenshot(mUri);
                    } catch (IOException e) {
                        Timber.d(e, "load screenshot error");
                    }
                    break;
                default:
            }
        }
    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {
        super.permissionGranted(permissionRequestCode, isGranted);

        if (!isGranted) {
            return;
        }

        switch (permissionRequestCode) {
            case IMAGE_REQUEST_CODE:
                try {
                    loadScreenshot(mUri);
                } catch (IOException e) {
                    Timber.d(e, "load screenshot error");
                }

                break;
        }
    }

    private void loadScreenshot(@Nullable Uri uri) throws IOException {
        Timber.d("load background image [%s]", uri);
        if (uri == null) {
            return;
        }
    }

    private void setImageCount() {
        mTvTitleImage.setText(String.format(getString(R.string.txt_attach_screen),
                String.valueOf(mAdapter.getItems().size())));
    }

    private void setEmail(String email) {
        if (mEdtEmail != null) {
            mEdtEmail.setText(email);
        }
    }

    private void collectInformation() {
        mCollector.installCollector(mUserCollector);
        mCollector.installCollector(mAppCollector);
        mCollector.installCollector(mDeviceCollector);
        mCollector.installCollector(new NetworkCollector(this.getActivity()));
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
    public void showError(String message) {
        showToast(message);
    }
}
