package vn.com.vng.zalopay.utils;


import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by huuhoa on 12/11/15.
 */
public abstract class VNDCurrencyTextWatcher implements TextWatcher {

    private EditText et;

    public VNDCurrencyTextWatcher(EditText et)
    {
        this.et = et;
    }

    @SuppressWarnings("unused")
    private static final String TAG = "VNDCurrencyTextWatcher";

    @Override
    public void afterTextChanged(Editable s)
    {
        long n = 0;
        et.removeTextChangedListener(this);

        try {
            int inilen, endlen;
            inilen = et.getText().length();
            String v = s.toString().replaceAll("[^\\d]","").trim();
            if ("".equals(v)){
                n=0;
            } else {
                n = Long.parseLong(v);
            }
            int cp = et.getSelectionStart();
            if (n <= 0){
                et.setText("");
            } else {
                et.setText(CurrencyUtil.formatCurrency(n, false));
            }

            endlen = et.getText().length();
            int sel = (cp + (endlen - inilen));
            if (sel > 0 && sel <= et.getText().length()) {
                et.setSelection(sel);
            } else {
                // place cursor at the end?
                et.setSelection(et.getText().length() > 1? et.getText().length() - 1 : 0);
            }


        } catch (NumberFormatException nfe) {
            // do nothing?
        }

        et.addTextChangedListener(this);
        onValueUpdate(n);
    }

    public abstract void onValueUpdate(long value);

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {

    }
}