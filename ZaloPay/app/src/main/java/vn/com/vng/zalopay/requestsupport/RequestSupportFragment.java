package vn.com.vng.zalopay.requestsupport;

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
import android.view.View;
import android.widget.TextView;

import com.zalopay.ui.widget.edittext.ZPEditText;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.AbsPickerImageFragment;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.feedback.FeedbackAdapter;
import vn.com.vng.zalopay.ui.widget.validate.EmailValidate;

public class RequestSupportFragment extends AbsPickerImageFragment implements IRequestSupportView,
        FeedbackAdapter.OnClickAddListener, FeedbackAdapter.OnClickDeleteListener {

    public static RequestSupportFragment newInstance() {
        return new RequestSupportFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_request_support;
    }

    private static final int IMAGE_REQUEST_CODE = 100;
    private static final int CATEGORY_REQUEST_CODE = 101;

    private FeedbackAdapter mAdapter;
    private AppResource mAppResource = new AppResource();

    @BindView(R.id.tvCategory)
    TextView mTvCategory;

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
    RequestSupportPresenter mPresenter;

    @Inject
    User mUser;

    @BindView(R.id.swSendUserInfor)
    SwitchCompat swSendUserInfor;

    @BindView(R.id.swSendDeviceInfor)
    SwitchCompat swSendDeviceInfor;

    @BindView(R.id.swSendAppInfor)
    SwitchCompat swSendAppInfor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAdapter = new FeedbackAdapter(getContext(), this, this);
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
            Uri uri = getPickImageResultUri(data, "screenshot.jpg");
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
        mBtnSendView.setEnabled(mEdtEmail.isValid() && mEdtDescribe.isValid() && mAppResource.appname != null);
    }

    @OnTextChanged(value = R.id.edtEmail, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChangedEmail() {
        mBtnSendView.setEnabled(mEdtEmail.isValid() && mEdtDescribe.isValid() && mAppResource.appname != null);
    }

    @OnFocusChange({R.id.edtEmail, R.id.edtDescribe})
    public void onFocusChange(View v, boolean hasView) {
        Timber.d("onFocusChange %s", hasView);
        mBtnSendView.setEnabled(mEdtEmail.isValid() && mEdtDescribe.isValid() && mAppResource.appname != null);
    }

    @OnClick(R.id.btnSend)
    public void onClickSend() {
        if (!mEdtEmail.validate() || !mEdtDescribe.validate()) {
            return;
        }

        if(mAppResource.appname == null) {
            return;
        }

        mPresenter.sendEmail(mTvCategory.getText().toString(),
                mEdtEmail.getText().toString(),
                mEdtDescribe.getText().toString(),
                swSendUserInfor.isChecked(),
                swSendAppInfor.isChecked(),
                swSendDeviceInfor.isChecked(), mAdapter.getItems());
    }

    @OnClick(R.id.containerCategory)
    public void onClickCategory() {
//        showCategoryBottomSheetDialog();
        Intent intent = new Intent(getContext(), ChooseCategoryActivity.class);
        startActivityForResult(intent, CATEGORY_REQUEST_CODE);
    }

    @Override
    public void onClickAdd(int position) {
        showScreenshotBottomSheetDialog(IMAGE_REQUEST_CODE);
    }

    @Override
    public void onClickDelete(int position) {
        removeScreenshot(position);
    }

    private void showScreenshotBottomSheetDialog(final int requestCode) {
        CoverBottomSheetDialogFragment dialog = CoverBottomSheetDialogFragment.newInstance();
        dialog.setOnClickListener(new CoverBottomSheetDialogFragment.OnClickListener() {
            @Override
            public void onClickCapture() {
                startCaptureImage(requestCode, "screenshot.jpg");
            }

            @Override
            public void onClickGallery() {
                startPickImage(requestCode);
            }
        });
        dialog.show(getChildFragmentManager(), "bottomsheet");
    }

    private void showCategoryBottomSheetDialog() {
        final CategoryBottomSheetDialogFragment dialog = CategoryBottomSheetDialogFragment.newInstance();
        dialog.setValues(setData());
        dialog.setOnClickListener(new CategoryBottomSheetDialogFragment.OnClickListener() {

            @Override
            public void onClickCancel() {
            }

            @Override
            public void onClickAccept() {
                mAppResource = dialog.getValue();
                mTvCategory.setText(mAppResource.appname);
            }
        });
        dialog.show(getChildFragmentManager(), "bottomsheet");
    }


    private void setImageCount() {
        String description = String.format(getString(R.string.txt_attach_screen),
                String.valueOf(mAdapter.getItems().size()));
        mTvTitleImage.setText(description);
    }

    private void setEmail(String email) {
        if (mEdtEmail != null) {
            mEdtEmail.setText(email);
        }
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

    private List<AppResource> setData() {
        List<AppResource> mAppResourceList = new ArrayList<>();

        AppResource tmp = new AppResource();
        tmp.appname = "Nạp tiền";
        AppResource tmp1 = new AppResource();
        tmp1.appname = "Rút tiền";
        AppResource tmp2 = new AppResource();
        tmp2.appname = "Chuyển tiền";
        AppResource tmp3 = new AppResource();
        tmp3.appname = "Nhận tiền";
        AppResource tmp4 = new AppResource();
        tmp4.appname = "Liên kết thẻ";

        mAppResourceList.add(tmp);
        mAppResourceList.add(tmp1);
        mAppResourceList.add(tmp2);
        mAppResourceList.add(tmp3);
        mAppResourceList.add(tmp4);

        return mAppResourceList;
    }
}
