package vn.com.vng.zalopay.passport.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.zalopay.ui.widget.edittext.NonSelectionActionModeCallback;

/**
 * Created by hieuvm on 6/16/17.
 * *
 */

public class CustomTextView extends AppCompatEditText implements NumberEditable {

    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCustomSelectionActionModeCallback(new NonSelectionActionModeCallback());
    }

    @Override
    public void append(int number) {
        append(String.valueOf(number));
    }

    @Override
    public void delete() {
        String text = getInputText();

        if (TextUtils.isEmpty(text)) {
            return;
        }

        text = text.substring(0, text.length() - 1);
        setInputText(text);
        setSelection(text.length());
    }

    @Override
    public String getInputText() {
        return super.getText().toString();
    }

    @Override
    public void setInputText(String text) {
        super.setText(text);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    /* @Override
    public boolean onCheckIsTextEditor() {
        return false;
    }

    @Override
    public boolean isTextSelectable() {
        return true;
    }*/
}
