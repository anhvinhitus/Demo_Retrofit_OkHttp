package vn.com.vng.zalopay.senderror;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zalopay.ui.widget.edittext.ZPEditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.AbsPickerImageFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.validate.EmailValidate;

public class SendErrorFragment extends BaseFragment implements
        SendErrorAdapter.OnClickAddListener, SendErrorAdapter.OnClickDeleteListener, View.OnClickListener {

    private SendErrorAdapter mAdapter;

    @BindView(R.id.tvTransactionType)
    TextView tvTransactionType;
    @BindView(R.id.edtTransactionId)
    ZPEditText edtTransactionId;
    @BindView(R.id.edtEmail)
    ZPEditText edtEmail;
    @BindView(R.id.edtDescribe)
    ZPEditText edtDescribe;

    @BindView(R.id.swSendUserInfor)
    SwitchCompat swSendUserInfor;
    @BindView(R.id.swSendDeviceInfor)
    SwitchCompat swSendDeviceInfor;
    @BindView(R.id.swSendAppInfor)
    SwitchCompat swSendAppInfor;

    @BindView(R.id.txtTitleImage)
    TextView tvTitleImage;
    @BindView(R.id.listView)
    RecyclerView mRecyclerView;

    @BindView(R.id.btnSend)
    Button btnSend;

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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        setTransactionType("Rút tiền");
        setTransactionId("160810000000064");
        List<Integer> tmp = new ArrayList<>();
        tmp.add(R.drawable.ic_agribank);
        tmp.add(R.drawable.ic_bidv);
        tmp.add(R.drawable.ic_chungchi);
//        tmp.add(R.drawable.ic_eximbank);
        setScreenshot(tmp);

        edtEmail.addValidator(new EmailValidate(getString(R.string.email_invalid)));

        edtDescribe.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edtDescribe.setFloatingLabelText(Html.fromHtml(
                        String.format(getResources().getString(R.string.describe_error_count),
                        String.valueOf(edtDescribe.getText().length()))));
            }
        });
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

    @Override
    public void onClick(View view) {
        int itemId = view.getId();
        if (itemId == R.id.swSendUserInfor) {

        } else if (itemId == R.id.swSendDeviceInfor) {

        } else if (itemId == R.id.swSendAppInfor) {

        } else if (itemId == R.id.btnSend) {

        }
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
        tvTransactionType.setText(type);
    }

    public void setTransactionId(String id) {
        edtTransactionId.setText(id);
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
        tvTitleImage.setText(String.format(getResources().getString(R.string.txt_attach_screen),
                String.valueOf(size)));
    }
}
