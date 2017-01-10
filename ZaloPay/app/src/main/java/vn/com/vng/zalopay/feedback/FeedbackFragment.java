package vn.com.vng.zalopay.feedback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.zalopay.ui.widget.edittext.ZPEditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.AbsPickerImageFragment;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.widget.validate.EmailValidate;

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

    @BindView(R.id.edtCategory)
    ZPEditText mCategoryView;

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

    @BindView(R.id.swSendUserInfor)
    SwitchCompat swSendUserInfor;

    @BindView(R.id.swSendDeviceInfor)
    SwitchCompat swSendDeviceInfor;

    @BindView(R.id.swSendAppInfor)
    SwitchCompat swSendAppInfor;

    private String mCategory;
    private String mTransactionId;
    private String mDescription;

    //compress format png
    @Nullable
    private byte[] mScreenshot;

    private String mScreenshotName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mAdapter = new FeedbackAdapter(getContext(), this, this);
        initArgs(getActivity().getIntent().getExtras());
        if (savedInstanceState != null) {
            mScreenshotName = savedInstanceState.getString("screenshotName");
        }
    }

    private void initArgs(Bundle bundle) {
        if (bundle == null) {
            return;
        }

        mCategory = bundle.getString("category");
        mTransactionId = bundle.getString("transactionid");
        mScreenshot = bundle.getByteArray("screenshot");
        mDescription = bundle.getString("description");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setBackgroundColor(Color.WHITE);

        mRecyclerView.setAdapter(mAdapter);

        mEdtEmail.addValidator(new EmailValidate(getString(R.string.email_invalid)));

        setEmail(mUser.email);
        setCategory(mCategory);
        setTransactionId(mTransactionId);
        setDescription(mDescription);

        if (mScreenshot != null) {
            mPresenter.insertScreenshot(mScreenshot);
        }

        setImageCount();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == IMAGE_REQUEST_CODE) {
            Uri uri = getPickImageResultUri(data, mScreenshotName);
            if (uri == null) {
                return;
            }

            insertScreenshot(uri);
        }
    }

    @Override
    public void insertScreenshot(Uri data) {
        mAdapter.insert(data, 0);
        setImageCount();
    }

    private void removeScreenshot(int position) {
        Uri screenshot = mAdapter.getItem(position);
        if (screenshot == null) {
            return;
        }

        mAdapter.remove(position);
        setImageCount();
        clearCacheFresco(screenshot);
    }

    @Override
    public void onDestroyView() {
        clearCacheFresco(mAdapter.getItems());
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

        mPresenter.sendEmail(mEdtTransactionId.getText().toString(),
                mCategoryView.getText().toString(),
                mEdtEmail.getText().toString(),
                mEdtDescribe.getText().toString(),
                swSendUserInfor.isChecked(),
                swSendAppInfor.isChecked(),
                swSendDeviceInfor.isChecked(), mAdapter.getItems());

    }

    @Override
    public void onClickAdd(int position) {
        showBottomSheetDialog(IMAGE_REQUEST_CODE, position);
    }

    @Override
    public void onClickDelete(int position) {
        removeScreenshot(position);
    }

    private void showBottomSheetDialog(final int requestCode, final int position) {
        CoverBottomSheetDialogFragment dialog = CoverBottomSheetDialogFragment.newInstance();
        dialog.setOnClickListener(new CoverBottomSheetDialogFragment.OnClickListener() {
            @Override
            public void onClickCapture() {
                startCaptureImage(requestCode, getScreenshotName(position));
            }

            @Override
            public void onClickGallery() {
                startPickImage(requestCode);
            }
        });
        dialog.show(getChildFragmentManager(), "bottomsheet");
    }

    private String getScreenshotName(int position) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH:mm:ss", Locale.getDefault());
        mScreenshotName = "screenshot-" + dateFormat.format(new Date()) + ".jpg";
        Timber.d("getScreenshotName: %s", mScreenshotName);
        return mScreenshotName;
    }

    private void setImageCount() {
        String description = String.format(getString(R.string.txt_attach_screen),
                String.valueOf(mAdapter.getItems().size()));
        mTvTitleImage.setText(description);
    }

    private void setEmail(String email) {
        if (mEdtEmail == null || TextUtils.isEmpty(email)) {
            return;
        }

        mEdtEmail.setClickable(false);
        mEdtEmail.setFocusable(false);
        mEdtEmail.setFocusableInTouchMode(false);
        mEdtEmail.setText(email);
    }

    private void setCategory(String category) {
        if (mCategoryView != null) {
            mCategoryView.setText(category);
        }
    }

    private void setTransactionId(String tranId) {
        if (mEdtTransactionId != null) {
            mEdtTransactionId.setText(tranId);
        }
    }

    private void setDescription(String message) {
        if (mEdtDescribe == null || TextUtils.isEmpty(message)) {
            return;
        }

        mEdtDescribe.setClickable(false);
        mEdtDescribe.setFocusable(false);
        mEdtDescribe.setFocusableInTouchMode(false);
        mEdtDescribe.setText(message);
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

    private void clearCacheFresco(Uri uri) {
        if (uri == null) {
            return;
        }
        Fresco.getImagePipeline().evictFromCache(uri);
    }

    private void clearCacheFresco(List<Uri> uris) {
        for (Uri uri : uris) {
            clearCacheFresco(uri);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(mScreenshotName)) {
            outState.putString("screenshotName", mScreenshotName);
        }
        super.onSaveInstanceState(outState);
    }
}
