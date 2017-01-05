package vn.com.vng.zalopay.senderror;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.zalopay.ui.widget.edittext.ZPEditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.AbsPickerImageFragment;
import vn.com.vng.zalopay.data.UserCollector;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.validate.EmailValidate;
import vn.zalopay.feedback.FeedbackCollector;
import vn.zalopay.feedback.collectors.AppCollector;
import vn.zalopay.feedback.collectors.DeviceCollector;
import vn.zalopay.feedback.collectors.NetworkCollector;

public class SendErrorFragment extends BaseFragment implements
        SendErrorAdapter.OnClickAddListener, SendErrorAdapter.OnClickDeleteListener, SwitchCompat.OnCheckedChangeListener {

    private SendErrorAdapter mAdapter;
    private FeedbackCollector mCollector;
    private UserCollector mUserCollector;
    private AppCollector mAppCollector;
    private DeviceCollector mDeviceCollector;

    @BindView(R.id.tvTransactionType)
    TextView mTvTransactionType;
    @BindView(R.id.edtTransactionId)
    ZPEditText mEdtTransactionId;
    @BindView(R.id.edtEmail)
    ZPEditText mEdtEmail;
    @BindView(R.id.edtDescribe)
    ZPEditText mEdtDescribe;

    @BindView(R.id.swSendUserInfor)
    SwitchCompat mSwSendUserInfor;
    @BindView(R.id.swSendDeviceInfor)
    SwitchCompat mSwSendDeviceInfor;
    @BindView(R.id.swSendAppInfor)
    SwitchCompat mSwSendAppInfor;

    @BindView(R.id.txtTitleImage)
    TextView mTvTitleImage;
    @BindView(R.id.listView)
    RecyclerView mRecyclerView;

    @BindView(R.id.btnSend)
    Button mBtnSend;

    public static SendErrorFragment newInstance() {
        return new SendErrorFragment();
    }

    @Override
    protected void setupFragmentComponent() {
//        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_send_error;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAdapter = new SendErrorAdapter(getContext(), this, this);

        mCollector = new FeedbackCollector();
        mUserCollector = new UserCollector(getAppComponent().userConfig());
        mAppCollector = new AppCollector(this.getActivity());
        mDeviceCollector = new DeviceCollector();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mSwSendUserInfor.setOnCheckedChangeListener(this);
        mSwSendDeviceInfor.setOnCheckedChangeListener(this);
        mSwSendAppInfor.setOnCheckedChangeListener(this);

        mEdtEmail.addValidator(new EmailValidate(getString(R.string.email_invalid)));

        collectInformation();
        setDefaultEmail();

        testData();
    }

    public void setScreenshot(List<Integer> images) {
        mAdapter.setData(images);
        setImageCount();
    }

    public void updateScreenshot(Integer image) {
        mAdapter.insert(image);
        setImageCount();
    }

    public void removeScreenshot(int position) {
        mAdapter.remove(position);
        setImageCount();
    }

    public void setTransactionType(String type) {
        if(type != null) {
            mTvTransactionType.setText(type);
        }
    }

    public void setTransactionId(String id) {
        if(id != null) {
            mEdtTransactionId.setText(id);
        }
    }

    @Override
    public void onDestroyView() {
//        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
//        mPresenter.destroy();
        mAdapter = null;
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
        mBtnSend.setEnabled(mEdtEmail.isValid() && mEdtDescribe.isValid());
    }

    @OnTextChanged(value = R.id.edtEmail, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChangedEmail() {
        mBtnSend.setEnabled(mEdtEmail.isValid() && mEdtDescribe.isValid());
    }

    @OnFocusChange({R.id.edtEmail, R.id.edtDescribe})
    public void onFocusChange(View v, boolean hasView) {
        Timber.d("onFocusChange %s", hasView);
        mBtnSend.setEnabled(mEdtEmail.isValid() && mEdtDescribe.isValid());
    }

    @OnClick(R.id.btnSend)
    public void onClickSend() {
        if (!mEdtEmail.validate() || !mEdtDescribe.validate()) {
            return;
        }

        mCollector.startCollectors();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int itemId = compoundButton.getId();
        if (itemId == R.id.swSendUserInfor) {
            if(b == false) {
                mCollector.removeCollector(mUserCollector);
            } else if(b == true) {
                mCollector.installCollector(mUserCollector);
            }
        } else if (itemId == R.id.swSendDeviceInfor) {
            if(b == false) {
                mCollector.removeCollector(mDeviceCollector);
            } else if(b == true) {
                mCollector.installCollector(mDeviceCollector);
            }
        } else if (itemId == R.id.swSendAppInfor) {
            if(b == false) {
                mCollector.removeCollector(mAppCollector);
            } else if(b == true) {
                mCollector.installCollector(mAppCollector);
            }
        }
    }

    @Override
    public void onClickAdd() {
        showBottomSheetDialog();
    }

    @Override
    public void onClickDelete(int position) {
        removeScreenshot(position);
    }

    private void showBottomSheetDialog() {
        AbsPickerImageFragment.CoverBottomSheetDialogFragment dialog = AbsPickerImageFragment.CoverBottomSheetDialogFragment.newInstance();
        dialog.setOnClickListener(new AbsPickerImageFragment.CoverBottomSheetDialogFragment.OnClickListener() {
            @Override
            public void onClickCapture() {
//                startCaptureImage(requestCode, getImageNameFromReqCode(requestCode));
            }

            @Override
            public void onClickGallery() {
//                startPickImage(requestCode);
            }
        });
        dialog.show(getChildFragmentManager(), "bottomsheet");
    }

    private void setImageCount() {
        int size = mAdapter.getItemCount() - 1;
        if(mAdapter.getItemViewType(size) == 0) {
            size = size + 1;
        }

        mTvTitleImage.setText(String.format(getResources().getString(R.string.txt_attach_screen),
                String.valueOf(size)));
    }

    private void setDefaultEmail() {
        UserConfig currentUser = getAppComponent().userConfig();
        String email = currentUser.getCurrentUser().email;
        if(email != null) {
            mEdtEmail.setText(email);
        }
    }

    private void collectInformation() {
        mCollector.installCollector(mUserCollector);
        mCollector.installCollector(mAppCollector);
        mCollector.installCollector(mDeviceCollector);
        mCollector.installCollector(new NetworkCollector(this.getActivity()));
    }

    private void testData() {
        setTransactionType("Rút tiền");
        setTransactionId("160810000000064");
        List<Integer> tmp = new ArrayList<>();
        tmp.add(R.drawable.ic_agribank);
//        tmp.add(R.drawable.ic_bidv);
//        tmp.add(R.drawable.ic_chungchi);
//        tmp.add(R.drawable.ic_eximbank);
        setScreenshot(tmp);
    }
}
