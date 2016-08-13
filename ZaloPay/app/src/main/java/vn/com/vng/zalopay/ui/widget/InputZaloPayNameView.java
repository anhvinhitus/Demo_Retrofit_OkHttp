package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 12/08/2016.
 * Control support for input, valid and show state (success/fail) of Zalo Pay name.
 */
public class InputZaloPayNameView extends FrameLayout {

    @BindView(R.id.textInputZaloPayName)
    TextInputLayout mTextInputZaloPayName;

    @BindView(R.id.edtZaloPayName)
    EditText mEdtZaloPayName;

    @BindView(R.id.imgInfo)
    ImageView mImgInfo;

    @BindView(R.id.tvCheck)
    TextView mTvCheck;

    private ZPNameStateEnum mCurrentState = ZPNameStateEnum.UNKNOWN;

    public InputZaloPayNameView(Context context) {
        super(context);
        initAttrs(context);
    }

    public InputZaloPayNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context);
    }

    public InputZaloPayNameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context);
    }

    private void initAttrs(Context context) {
        inflate(context, R.layout.input_zalopay_name, this);
        ButterKnife.bind(this, this);
    }

    @OnClick(R.id.imgInfo)
    public void onClickIconInfo(View v) {
        if (mCurrentState == ZPNameStateEnum.INVALID) {
            mEdtZaloPayName.getText().clear();
            showImgInfo();
        } else if (mCurrentState == ZPNameStateEnum.UNKNOWN) {
            showDialogZaloPayName();
        }
    }

    @OnTextChanged(R.id.edtZaloPayName)
    public void onTextChangeAccountName(CharSequence s) {
        mCurrentState = ZPNameStateEnum.UNKNOWN;
        if (TextUtils.isEmpty(s)) {
            showImgInfo();
        } else if (validZPName()) {
            showBtnCheck();
        } else {
            showImgInfo();
        }
    }


    public ZPNameStateEnum getCurrentState() {
        return mCurrentState;
    }

    public String getText() {
        return mEdtZaloPayName.getText().toString();
    }

    public int length() {
        return mEdtZaloPayName.length();
    }

    public void setOnClickBtnCheck(OnClickListener onClickBtnCheck) {
        if (mTvCheck == null) {
            return;
        }
        mTvCheck.setOnClickListener(onClickBtnCheck);
    }

    private void showImgInfo() {
        mTextInputZaloPayName.setHint(getContext().getString(R.string.hint_zalopay_name));
        hideZPNameError();

        mCurrentState = ZPNameStateEnum.UNKNOWN;
        mImgInfo.setVisibility(VISIBLE);
        mTvCheck.setVisibility(GONE);
        showIconState(mCurrentState);
    }

    public void showBtnCheck() {
        hideZPNameError();
        mTextInputZaloPayName.setHint(getContext().getString(R.string.hint_zalopay_name));

        mImgInfo.setVisibility(GONE);
        mTvCheck.setVisibility(VISIBLE);
    }

    public void showCheckFail() {
        showZPNameError("Tên tài khoản đã được sử dụng");

        mCurrentState = ZPNameStateEnum.INVALID;
        mImgInfo.setVisibility(VISIBLE);
        mTvCheck.setVisibility(GONE);
        showIconState(mCurrentState);
    }

    public void showCheckSuccess() {

        mTextInputZaloPayName.setError(null);
        mTextInputZaloPayName.setHint(getContext().getString(R.string.valid_zalopay_name));

        mCurrentState = ZPNameStateEnum.VALID;
        mImgInfo.setVisibility(VISIBLE);
        mTvCheck.setVisibility(GONE);
        showIconState(mCurrentState);
    }

    public boolean validZPName() {
        String zaloPayName = mEdtZaloPayName.getText().toString();
        if (TextUtils.isEmpty(zaloPayName)) {
            hideZPNameError();
            showBtnCheck();
            return true;
        } else if (!ValidateUtil.isValidLengthZPName(zaloPayName)) {
            showZPNameError(getContext().getString(R.string.exception_account_name_length));
            return false;
        } else if (!ValidateUtil.isValidZaloPayName(zaloPayName)) {
            showZPNameError(getContext().getString(R.string.exception_account_name_special_char));
            return false;
        } else {
            hideZPNameError();
            showBtnCheck();
            return true;
        }
    }

    private void showZPNameError(String error) {
        if (!TextUtils.isEmpty(error)) {
            mTextInputZaloPayName.setErrorEnabled(true);
            mTextInputZaloPayName.setError(error);
        } else {
            hideZPNameError();
        }
    }

    private void hideZPNameError() {
        mTextInputZaloPayName.setErrorEnabled(false);
        mTextInputZaloPayName.setError(null);
    }

    private void showDialogZaloPayName() {
        new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog)
                .setContentText(getContext().getString(R.string.hint_edit_account))
                .setConfirmText("Đóng")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                })
                .show();
    }

    public enum ZPNameStateEnum {
        UNKNOWN(0),
        VALID(1),
        INVALID(2);

        int value;

        ZPNameStateEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private void showIconState(ZPNameStateEnum state) {
        if (state == ZPNameStateEnum.UNKNOWN) {
            mImgInfo.setImageResource(R.drawable.ic_info);
        } else if (state == ZPNameStateEnum.INVALID) {
            mImgInfo.setImageResource(R.drawable.ic_check_fail);
        } else {
            mImgInfo.setImageResource(R.drawable.ic_checked_mark);
        }
    }
}
