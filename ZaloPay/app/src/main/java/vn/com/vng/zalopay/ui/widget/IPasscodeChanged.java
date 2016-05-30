package vn.com.vng.zalopay.ui.widget;

import android.text.Editable;

/**
 * Created by longlv on 30/05/2016.
 */
public interface IPasscodeChanged {
    void beforeTextChanged(CharSequence s, int start, int count, int after);
    void onTextChanged(CharSequence s, int start, int before, int count);
    void afterTextChanged(Editable s);
}
