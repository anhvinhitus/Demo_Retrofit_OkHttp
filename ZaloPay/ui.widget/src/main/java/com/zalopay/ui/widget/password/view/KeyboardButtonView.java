package com.zalopay.ui.widget.password.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zalopay.ui.widget.R;
import com.zalopay.ui.widget.password.interfaces.KeyboardButtonClickedListener;

public class KeyboardButtonView extends RelativeLayout implements View.OnClickListener {

    private KeyboardButtonClickedListener mKeyboardButtonClickedListener;

    private Context mContext;
    private RelativeLayout mRippleView;
    private int backgroundResource;

    public KeyboardButtonView(Context context) {
        this(context, null);
    }

    public KeyboardButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyboardButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.mContext = context;
        initializeView(attrs, defStyleAttr);
    }

    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null && !isInEditMode()) {
            final TypedArray attributes = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.KeyboardButtonView,
                    defStyleAttr, 0);
            String text = attributes.getString(R.styleable.KeyboardButtonView_lp_keyboard_button_text);
            Drawable image = attributes.getDrawable(R.styleable.KeyboardButtonView_lp_keyboard_button_image);
            boolean rippleEnabled = attributes.getBoolean(R.styleable.KeyboardButtonView_lp_keyboard_button_ripple_enabled, true);

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            KeyboardButtonView view = (KeyboardButtonView) inflater.inflate(R.layout.view_pin_keyboard_button, this);

            if (text != null) {
                TextView textView = (TextView) view.findViewById(R.id.keyboard_button_textview);
                if (textView != null) {
                    textView.setText(text);
                }
            }
            if (image != null) {
                ImageView imageView = (ImageView) view.findViewById(R.id.keyboard_button_imageview);
                if (imageView != null) {
                    imageView.setImageDrawable(image);
                    imageView.setVisibility(View.VISIBLE);
                }
            }

            mRippleView = (RelativeLayout) view.findViewById(R.id.pin_code_keyboard_button_ripple);
            int[] attr = new int[]{R.attr.selectableItemBackgroundBorderless};
            TypedArray typedArray = mContext.obtainStyledAttributes(attr);
            backgroundResource = typedArray.getResourceId(0, 0);
            mRippleView.setBackgroundResource(backgroundResource);
            mRippleView.setOnClickListener(this);
            if (mRippleView != null) {
                if (!rippleEnabled) {
                    mRippleView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void setOnRippleAnimationEndListener(KeyboardButtonClickedListener keyboardButtonClickedListener) {
        mKeyboardButtonClickedListener = keyboardButtonClickedListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        onTouchEvent(event);
        return false;
    }

    @Override
    public void onClick(View v) {
        if (mKeyboardButtonClickedListener != null) {
            mKeyboardButtonClickedListener.onRippleAnimationEnd();
        }
    }
}
