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
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import vn.com.vng.zalopay.ui.widget.validate.EmailValidate;

public class FeedbackFragment extends AbsPickerImageFragment implements IFeedbackView,
        FeedbackAdapter.OnClickAddListener, FeedbackAdapter.OnClickDeleteListener,
        FeedbackAdapter.OnClickImageListener {

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

    private int mScreenshotPos;
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

    @BindView(R.id.screenshotView)
    LinearLayout mView;

    @BindView(R.id.screenshot)
    ImageView mScreenshotView;

    @Inject
    FeedbackPresenter mPresenter;

    @BindView(R.id.swSendUserInfor)
    SwitchCompat swSendUserInfor;

    @BindView(R.id.swSendDeviceInfor)
    SwitchCompat swSendDeviceInfor;

    @BindView(R.id.swSendAppInfor)
    SwitchCompat swSendAppInfor;

    private String mScreenshotName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FeedbackAdapter(getContext(), this, this, this);

        if (savedInstanceState != null) {
            mScreenshotName = savedInstanceState.getString("screenshotName");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setBackgroundColor(Color.WHITE);

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
        mEdtEmail.addValidator(new EmailValidate(getString(R.string.email_invalid)));
        mPresenter.onViewCreated();
    }

    private RecyclerView.AdapterDataObserver mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            notifyImageCountChange();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyImageCountChange();
        }


    };

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
    }

    private void removeScreenshot(int position) {
        Uri screenshot = mAdapter.getItem(position);
        if (screenshot == null) {
            return;
        }

        mAdapter.remove(position);
        clearCacheFresco(screenshot);
    }

    @Override
    public void onDestroyView() {
        mAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
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


        Timber.d("onClickSend: %s %s", mEdtEmail.validate(), mEdtDescribe.validate());

        if (!mEdtEmail.validate() || !mEdtDescribe.validate()) {
            return;
        }

        mPresenter.collectAndSend(mEdtEmail.getText().toString(),
                mEdtDescribe.getText().toString(),
                swSendUserInfor.isChecked(),
                swSendAppInfor.isChecked(),
                swSendDeviceInfor.isChecked(), mAdapter.getItems());

    }

    @OnClick(R.id.tvCancel)
    public void onClickScreenshotCancel() {
        mView.setVisibility(View.GONE);
    }

    @OnClick(R.id.tvDelete)
    public void onClickScreenshotDelete() {
        removeScreenshot(mScreenshotPos);
        mView.setVisibility(View.GONE);
    }

    @Override
    public void onClickAdd(int position) {
        showBottomSheetDialog(IMAGE_REQUEST_CODE, position);
    }

    @Override
    public void onClickDelete(int position) {
        removeScreenshot(position);
    }

    @Override
    public void onClickImage(int position) {
        mView.setVisibility(View.VISIBLE);
        mScreenshotPos = position;
        mScreenshotView.setImageURI(mAdapter.getItem(mScreenshotPos));
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

    private void notifyImageCountChange() {
        if (mTvTitleImage == null) {
            return;
        }
        
        String description = String.format(getString(R.string.txt_attach_screen),
                String.valueOf(mAdapter.getItems().size()));
        mTvTitleImage.setText(description);
    }

    public void setEmail(String email) {
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

    public void setTransaction(String category, String transId, String errorMessage) {
        setCategory(category);
        setTransactionId(transId);
        setDescription(errorMessage);
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

    @Override
    public void finish() {
        getActivity().finish();
    }
}
