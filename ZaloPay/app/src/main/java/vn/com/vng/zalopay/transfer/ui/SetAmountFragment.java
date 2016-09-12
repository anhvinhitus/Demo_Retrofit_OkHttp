package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.VNDCurrencyTextWatcher;

/**
 * Created by AnhHieu on 8/29/16.
 * *
 */
public class SetAmountFragment extends BaseFragment {

    public static SetAmountFragment newInstance() {

        Bundle args = new Bundle();

        SetAmountFragment fragment = new SetAmountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_set_amount;
    }

    @BindView(R.id.textInputAmount)
    TextInputLayout textInputAmountView;

    @BindView(R.id.textInputMessage)
    TextInputLayout textInputMessageView;

    public long mAmount;

    @OnClick(R.id.btnUpdate)
    public void onClickUpdate() {


        if (mAmount < Constants.MIN_TRANSFER_MONEY) {
            textInputAmountView.setError(String.format(getString(R.string.min_money), Constants.MIN_TRANSFER_MONEY));
            return;
        }

        if (mAmount > Constants.MAX_TRANSFER_MONEY) {
            textInputAmountView.setError(String.format(getString(R.string.max_money), Constants.MAX_TRANSFER_MONEY));
            return;
        }


        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putLong("amount", mAmount);
        EditText editText = textInputMessageView.getEditText();
        if (editText != null) {
            bundle.putString("message", editText.getText().toString());
        }
        data.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText editText = textInputAmountView.getEditText();
        if (editText != null) {
            editText.addTextChangedListener(new VNDCurrencyTextWatcher(editText) {
                @Override
                public void onValueUpdate(long value) {
                    mAmount = value;
                }
            });
        }
    }

    @OnTextChanged(R.id.edtAmount)
    public void onTextChanged(CharSequence s) {
        textInputAmountView.setError("");
    }
}
