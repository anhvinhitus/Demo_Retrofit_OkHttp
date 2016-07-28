package vn.com.vng.zalopay.balancetopup.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.vng.zalopay.utils.VNDCurrencyTextWatcher;

/**
 * Created by longlv on 09/05/2016.
 */
public class InputAmountLayout extends LinearLayout {

    private IListenerAmountChanged mListener;
    private long mAmount;

    @BindView(R.id.layoutAmount)
    View layoutAmount;

    @BindView(R.id.tvHintInputAmount)
    TextView tvHintInputAmount;

    @BindView(R.id.layoutInputAmount)
    View layoutInputAmount;

    @BindView(R.id.tvAmountTitle)
    TextView tvAmountTitle;

    @BindView(R.id.tvCurrency)
    TextView tvCurrency;

    @BindView(R.id.edtAmount)
    EditText edtAmount;

    @BindView(R.id.imgClear)
    ImageView imgClear;

    @OnClick(R.id.layoutAmount)
    public void onClickLayoutAmount(View view) {
        edtAmount.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edtAmount, InputMethodManager.SHOW_IMPLICIT);
        onFocusChangeEdtAmount(edtAmount, true);
    }

    @OnClick(R.id.imgClear)
    public void onClickClear(View view) {
        edtAmount.setText("");
    }

    @OnTextChanged(R.id.edtAmount)
    public void onEdtAmountTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (text != null && text.length() > 0) {
            imgClear.setVisibility(VISIBLE);
        } else {
            imgClear.setVisibility(GONE);
        }
        if (mListener != null) {
            mListener.onAmountChanged(text);
        }
    }

    @OnFocusChange(R.id.edtAmount)
    public void onFocusChangeEdtAmount(View v, boolean hasFocus) {
        String amount = edtAmount.getText().toString();
        if (hasFocus) {
                tvAmountTitle.setTextColor(AndroidUtils.getColor(getContext(), R.color.text_green));
                tvCurrency.setTextColor(AndroidUtils.getColor(getContext(), R.color.text_green));
                tvHintInputAmount.setVisibility(View.GONE);
                layoutInputAmount.setVisibility(View.VISIBLE);
        } else {
            if (TextUtils.isEmpty(amount)) {
                layoutInputAmount.setVisibility(View.GONE);
                tvHintInputAmount.setVisibility(View.VISIBLE);
            } else {
                tvAmountTitle.setTextColor(AndroidUtils.getColor(getContext(), R.color.black));
                tvCurrency.setTextColor(AndroidUtils.getColor(getContext(), R.color.black));
                tvHintInputAmount.setVisibility(View.GONE);
                layoutInputAmount.setVisibility(View.VISIBLE);
            }
        }
    }

    public void requestFocusEdittext() {
        edtAmount.requestFocus();
    }

    public long getAmount() {
        return mAmount;
    }

    public String getText() {
        return edtAmount.getText().toString();
    }

    public void setListener(IListenerAmountChanged listener) {
        mListener = listener;
    }

    public void removeListener() {
        mListener = null;
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.input_amount_layout, this);
        ButterKnife.bind(this, view);
        edtAmount.addTextChangedListener(new VNDCurrencyTextWatcher(edtAmount) {
            @Override
            public void onValueUpdate(long value) {
                mAmount = value;
            }

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                showError(null);
            }
        });
    }


    private void initView(Context context, AttributeSet attrs) {
        initView(context);
    }

    public InputAmountLayout(Context context) {
        super(context);
        initView(context);
    }

    public InputAmountLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }


    public InputAmountLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InputAmountLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    public interface IListenerAmountChanged {
        void onAmountChanged(CharSequence amount);
    }

    protected void showError(String message){
        if (!TextUtils.isEmpty(message)){
           ToastUtil.showToast(getContext(), message);
        }
    }
}
