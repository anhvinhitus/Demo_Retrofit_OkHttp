package vn.com.vng.zalopay.balancetopup.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by longlv on 09/05/2016.
 */
public class InputAmountLayout extends LinearLayout {

    private IListenerAmountChanged mListener;

    @Bind(R.id.layoutAmount)
    View layoutAmount;

    @Bind(R.id.tvHintInputAmount)
    TextView tvHintInputAmount;

    @Bind(R.id.layoutInputAmount)
    View layoutInputAmount;

    @Bind(R.id.tvAmountTitle)
    TextView tvAmountTitle;

    @Bind(R.id.tvCurrency)
    TextView tvCurrency;

    @Bind(R.id.edtAmount)
    EditText edtAmount;

    @Bind(R.id.imgClear)
    ImageView imgClear;

    @OnClick(R.id.layoutAmount)
    public void onClickLayoutAmount(View view) {
        edtAmount.requestFocus();
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
            if (TextUtils.isEmpty(amount)) {
                tvHintInputAmount.setVisibility(View.VISIBLE);
                layoutInputAmount.setVisibility(View.GONE);
            } else {
                tvAmountTitle.setTextColor(AndroidUtils.getColor(getContext(), R.color.green));
                tvCurrency.setTextColor(AndroidUtils.getColor(getContext(), R.color.green));
                tvHintInputAmount.setVisibility(View.GONE);
                layoutInputAmount.setVisibility(View.VISIBLE);
            }
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

    public String getText() {
        return edtAmount.getText().toString();
    }

    public void setListener(IListenerAmountChanged listener) {
        mListener = listener;
    }

    public void removeListener() {
        mListener = null;
    }

    public InputAmountLayout(Context context) {
        super(context);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.input_amount_layout, null);
        ButterKnife.bind(this, view);
        this.addView(view);
    }

    public InputAmountLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InputAmountLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InputAmountLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public interface IListenerAmountChanged {
        public void onAmountChanged(CharSequence amount);
    }
}
