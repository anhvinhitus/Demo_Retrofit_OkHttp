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

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 12/08/2016.
 * Control support for input, valid and show state (success/fail) of Zalo Pay name.
 */
public class InputZaloPayNameView extends FrameLayout {
    private TextInputLayout mTextInputZaloPayName;
    private EditText mEdtZaloPayName;
    private ImageView mImgInfo;
    private ImageView mImgSuccess;
    private ImageView mImgFail;
    private TextView mTvCheck;
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
        mTextInputZaloPayName = (TextInputLayout) this.findViewById(R.id.textInputZaloPayName);
        mEdtZaloPayName = (EditText) this.findViewById(R.id.edtZaloPayName);
        mImgInfo = (ImageView) this.findViewById(R.id.imgInfo);
        mImgSuccess = (ImageView) this.findViewById(R.id.imgSuccess);
        mImgFail = (ImageView) this.findViewById(R.id.imgFail);
        mTvCheck = (TextView) this.findViewById(R.id.tvCheck);

        mImgFail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtZaloPayName.getText().clear();
                showImgInfo();
            }
        });

        mEdtZaloPayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCurrentState = ZPNameStateEnum.UNKNOWN;
                if (TextUtils.isEmpty(s)) {
                    showImgInfo();
                } else if (validZPName()) {
                    showBtnCheck();
                } else {
                    showImgInfo();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mImgInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogZaloPayName();
            }
        });
    }

    public ZPNameStateEnum getCurrentState() {
        return mCurrentState;
    }

    public String getString() {
        return mEdtZaloPayName.getText().toString();
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
        mImgInfo.setVisibility(VISIBLE);
        mTvCheck.setVisibility(GONE);
        mImgSuccess.setVisibility(GONE);
        mImgFail.setVisibility(GONE);
    }

    public void showBtnCheck() {
        hideZPNameError();
        mTextInputZaloPayName.setHint(getContext().getString(R.string.hint_zalopay_name));
        mImgInfo.setVisibility(GONE);
        mTvCheck.setVisibility(VISIBLE);
        mImgSuccess.setVisibility(GONE);
        mImgFail.setVisibility(GONE);
    }

    public void showCheckFail() {
        mCurrentState = ZPNameStateEnum.UNVALID;
        showZPNameError("Tên tài khoản đã được sử dụng");
        mImgInfo.setVisibility(GONE);
        mTvCheck.setVisibility(GONE);
        mImgSuccess.setVisibility(GONE);
        mImgFail.setVisibility(VISIBLE);
    }

    public void showCheckSuccess() {
        mCurrentState = ZPNameStateEnum.VALID;
        mTextInputZaloPayName.setError(null);
        mTextInputZaloPayName.setHint(getContext().getString(R.string.valid_zalopay_name));
        mImgInfo.setVisibility(GONE);
        mTvCheck.setVisibility(GONE);
        mImgSuccess.setVisibility(VISIBLE);
        mImgFail.setVisibility(GONE);
    }

    public boolean validZPName() {
        String zaloPayName = mEdtZaloPayName.getText().toString();
        if (TextUtils.isEmpty(zaloPayName)) {
            hideZPNameError();
            showBtnCheck();
            return true;
        } else if (!ValidateUtil.isValidLengthZPName(zaloPayName)) {
            showZPNameError("Tên tài khoản phải từ 4-24 ký tự");
            return false;
        } else if (!ValidateUtil.isValidZaloPayName(zaloPayName)) {
            showZPNameError("Tên tài khoản chỉ chứa các ký tự từ A-Z, a-z, 0-9");
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
        UNVALID(2);

        int value;

        ZPNameStateEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
