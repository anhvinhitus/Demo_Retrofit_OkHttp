package vn.com.vng.zalopay.passport.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.zalopay.ui.widget.password.enums.KeyboardButtonEnum;
import com.zalopay.ui.widget.password.interfaces.KeyboardButtonClickedListener;
import com.zalopay.ui.widget.password.view.KeyboardView;

/**
 * Created by hieuvm on 6/16/17.
 * *
 */

public class CustomKeyboardView extends KeyboardView implements KeyboardButtonClickedListener {
    public CustomKeyboardView(Context context) {
        this(context, null);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context, attrs);
    }

    private void initializeView(Context context, AttributeSet attrs) {
        setKeyboardButtonClickedListener(this);
    }

    private NumberEditable mNumberEditable;

    public void setEditable(NumberEditable view) {
        this.mNumberEditable = view;
    }

    @Override
    public void onKeyboardClick(KeyboardButtonEnum keyboardButtonEnum) {
        if (keyboardButtonEnum == KeyboardButtonEnum.BUTTON_CLEAR) {
            if (mNumberEditable != null) {
                mNumberEditable.delete();
            }
        } else {
            if (mNumberEditable != null) {
                mNumberEditable.append(keyboardButtonEnum.getButtonValue());
            }
        }
    }

    @Override
    public void onRippleAnimationEnd() {

    }
}
