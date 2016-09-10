package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
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
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 12/08/2016.
 * Control support for input, valid and show state (success/fail) of Zalo Pay name.
 */
public class InputZaloPayNameView extends FrameLayout {

    private InputZaloPayNameListener mListener;
    private OnClickListener mOnClickBtnCheck;

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
        mEdtZaloPayName.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mListener != null) {
                    mListener.onFocusChange(hasFocus);
                }
            }
        });
    }

    @Override
    public void setOnKeyListener(OnKeyListener l) {
        if (mEdtZaloPayName != null) {
            mEdtZaloPayName.setOnKeyListener(l);
        } else {
            super.setOnKeyListener(l);
        }
    }

    @OnClick(R.id.imgInfo)
    public void onClickIconInfo(View v) {
        if (mCurrentState == ZPNameStateEnum.INVALID) {
            mEdtZaloPayName.getText().clear();
            //showImgInfo();
            disableBtnCheck();
        } else if (mCurrentState == ZPNameStateEnum.UNKNOWN) {
            showDialogZaloPayName();
        }
    }

    @OnTextChanged(R.id.edtZaloPayName)
    public void onTextChangeAccountName(CharSequence s) {
        mCurrentState = ZPNameStateEnum.UNKNOWN;
        if (TextUtils.isEmpty(s)) {
            //showImgInfo();
            hideZPNameError();
            disableBtnCheck();
        } else if (validZPName()) {
            enableBtnCheck();
        }
        if (mListener != null) {
            mListener.onTextChanged(s);
        }
    }

    public void setOntextChangeListener(InputZaloPayNameListener listener) {
        mListener = listener;
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
        mOnClickBtnCheck = onClickBtnCheck;
        mTvCheck.setOnClickListener(mOnClickBtnCheck);
    }

    private void showImgInfo() {
        mTextInputZaloPayName.setHint(getContext().getString(R.string.hint_zalopay_name));
        hideZPNameError();

        mCurrentState = ZPNameStateEnum.UNKNOWN;
        mImgInfo.setVisibility(VISIBLE);
        mTvCheck.setVisibility(GONE);
        showIconState(mCurrentState);
    }

    private void enableBtnCheck() {
        mImgInfo.setVisibility(GONE);
        mTvCheck.setTextColor(AndroidUtils.getColor(getContext(), R.color.txt_check_enable));
        mTvCheck.setVisibility(VISIBLE);
        mTvCheck.setEnabled(true);
    }

    private void disableBtnCheck() {
        mImgInfo.setVisibility(GONE);
        mTvCheck.setTextColor(AndroidUtils.getColor(getContext(), R.color.txt_check_disable));
        mTvCheck.setVisibility(VISIBLE);
        mTvCheck.setEnabled(false);
    }

    public void showCheckFail() {
        showZPNameError(getContext().getString(R.string.account_existed));

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
            enableBtnCheck();
            return true;
        } else if (!ValidateUtil.isValidLengthZPName(zaloPayName)) {
            showZPNameError(getContext().getString(R.string.exception_account_name_length));
            disableBtnCheck();
            return false;
        } else if (!ValidateUtil.isValidZaloPayName(zaloPayName)) {
            showZPNameError(getContext().getString(R.string.exception_account_name_special_char));
            disableBtnCheck();
            return false;
        } else {
            hideZPNameError();
            enableBtnCheck();
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
