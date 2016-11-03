package com.zalopay.ui.widget.edittext;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.zalopay.ui.widget.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hieuvm on 11/3/16.
 */

public class ZPEditText extends AppCompatEditText {

    @IntDef({FLOATING_LABEL_NONE, FLOATING_LABEL_NORMAL, FLOATING_LABEL_HIGHLIGHT})
    public @interface FloatingLabelType {
    }

    public static final int FLOATING_LABEL_NONE = 0;
    public static final int FLOATING_LABEL_NORMAL = 1;
    public static final int FLOATING_LABEL_HIGHLIGHT = 2;

    private int extraPaddingTop;

    private int extraPaddingBottom;

    private int extraPaddingLeft;

    private int extraPaddingRight;

    private int floatingLabelTextSize;

    private int floatingLabelTextColor;

    private int bottomTextSize;

    private int floatingLabelPadding;

    private int bottomSpacing;

    private boolean floatingLabelEnabled;

    private boolean highlightFloatingLabel;

    private int baseColor;

    private int innerPaddingTop;

    private int innerPaddingBottom;

    private int innerPaddingLeft;

    private int innerPaddingRight;

    private int primaryColor;

    private int errorColor;

    private int minCharacters;

    private int maxCharacters;

    private boolean singleLineEllipsis;

    private boolean floatingLabelAlwaysShown;

    private boolean helperTextAlwaysShown;

    private int bottomEllipsisSize;

    private int minBottomLines;

    private int minBottomTextLines;

    private float currentBottomLines;

    private float bottomLines;

    private String helperText;

    private int helperTextColor = -1;

    private String tempErrorText;

    private float floatingLabelFraction;

    private boolean floatingLabelShown;

    private float focusFraction;

    private Typeface accentTypeface;

    private Typeface typeface;

    private CharSequence floatingLabelText;

    private boolean hideUnderline;

    private int underlineColor;

    private boolean autoValidate;

    private boolean charactersCountValid;

    private boolean floatingLabelAnimating;

    private boolean checkCharactersCountAtBeginning;

    private Bitmap[] iconLeftBitmaps;

    private Bitmap[] iconRightBitmaps;

    private Bitmap[] clearButtonBitmaps;

    private boolean validateOnFocusLost;

    private boolean showClearButton;
    private boolean firstShown;
    private int iconSize;
    private int iconOuterWidth;
    private int iconOuterHeight;
    private int iconPadding;
    private boolean clearButtonTouched;
    private boolean clearButtonClicking;
    private ColorStateList textColorStateList;
    private ColorStateList textColorHintStateList;
    private ArgbEvaluator focusEvaluator = new ArgbEvaluator();
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    StaticLayout textLayout;
    ObjectAnimator labelAnimator;
    ObjectAnimator labelFocusAnimator;
    ObjectAnimator bottomLinesAnimator;
    OnFocusChangeListener innerFocusChangeListener;
    OnFocusChangeListener outerFocusChangeListener;
    private List<ZPEditTextValidate> validators;
    private ZPEditTextLengthChecker lengthChecker;

    public ZPEditText(Context context) {
        super(context);
        init(context, null);
    }

    public ZPEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ZPEditText(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        iconSize = getPixel(32);
        iconOuterWidth = getPixel(48);
        iconOuterHeight = getPixel(32);

        bottomSpacing = getResources().getDimensionPixelSize(R.dimen.inner_components_spacing);
        bottomEllipsisSize = getResources().getDimensionPixelSize(R.dimen.bottom_ellipsis_height);

        // default baseColor is black
        int defaultBaseColor = Color.BLACK;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ZPEditText);
        textColorStateList = typedArray.getColorStateList(R.styleable.ZPEditText_zlp_textColor);
        textColorHintStateList = typedArray.getColorStateList(R.styleable.ZPEditText_zlp_textColorHint);
        baseColor = typedArray.getColor(R.styleable.ZPEditText_zlp_baseColor, defaultBaseColor);

        int defaultPrimaryColor;
        TypedValue primaryColorTypedValue = new TypedValue();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context.getTheme().resolveAttribute(android.R.attr.colorPrimary, primaryColorTypedValue, true);
                defaultPrimaryColor = primaryColorTypedValue.data;
            } else {
                throw new RuntimeException("SDK_INT less than LOLLIPOP");
            }
        } catch (Exception e) {
            try {
                int colorPrimaryId = getResources().getIdentifier("colorPrimary", "attr", getContext().getPackageName());
                if (colorPrimaryId != 0) {
                    context.getTheme().resolveAttribute(colorPrimaryId, primaryColorTypedValue, true);
                    defaultPrimaryColor = primaryColorTypedValue.data;
                } else {
                    throw new RuntimeException("colorPrimary not found");
                }
            } catch (Exception e1) {
                defaultPrimaryColor = baseColor;
            }
        }

        primaryColor = typedArray.getColor(R.styleable.ZPEditText_zlp_primaryColor, defaultPrimaryColor);
        setFloatingLabelInternal(typedArray.getInt(R.styleable.ZPEditText_zlp_floatingLabel, 0));
        errorColor = typedArray.getColor(R.styleable.ZPEditText_zlp_errorColor, Color.parseColor("#e7492E"));
        minCharacters = typedArray.getInt(R.styleable.ZPEditText_zlp_minCharacters, 0);
        maxCharacters = typedArray.getInt(R.styleable.ZPEditText_zlp_maxCharacters, 0);
        singleLineEllipsis = typedArray.getBoolean(R.styleable.ZPEditText_zlp_singleLineEllipsis, false);
        helperText = typedArray.getString(R.styleable.ZPEditText_zlp_helperText);
        helperTextColor = typedArray.getColor(R.styleable.ZPEditText_zlp_helperTextColor, -1);
        minBottomTextLines = typedArray.getInt(R.styleable.ZPEditText_zlp_minBottomTextLines, 0);
        String fontPathForAccent = typedArray.getString(R.styleable.ZPEditText_zlp_accentTypeface);
        if (fontPathForAccent != null && !isInEditMode()) {
            accentTypeface = getCustomTypeface(fontPathForAccent);
            textPaint.setTypeface(accentTypeface);
        }
        String fontPathForView = typedArray.getString(R.styleable.ZPEditText_zlp_typeface);
        if (fontPathForView != null && !isInEditMode()) {
            typeface = getCustomTypeface(fontPathForView);
            setTypeface(typeface);
        }
        floatingLabelText = typedArray.getString(R.styleable.ZPEditText_zlp_floatingLabelText);
        if (floatingLabelText == null) {
            floatingLabelText = getHint();
        }
        floatingLabelPadding = typedArray.getDimensionPixelSize(R.styleable.ZPEditText_zlp_floatingLabelPadding, bottomSpacing);
        floatingLabelTextSize = typedArray.getDimensionPixelSize(R.styleable.ZPEditText_zlp_floatingLabelTextSize, getResources().getDimensionPixelSize(R.dimen.floating_label_text_size));
        floatingLabelTextColor = typedArray.getColor(R.styleable.ZPEditText_zlp_floatingLabelTextColor, -1);
        floatingLabelAnimating = typedArray.getBoolean(R.styleable.ZPEditText_zlp_floatingLabelAnimating, true);
        bottomTextSize = typedArray.getDimensionPixelSize(R.styleable.ZPEditText_zlp_bottomTextSize, getResources().getDimensionPixelSize(R.dimen.bottom_text_size));
        hideUnderline = typedArray.getBoolean(R.styleable.ZPEditText_zlp_hideUnderline, false);
        underlineColor = typedArray.getColor(R.styleable.ZPEditText_zlp_underlineColor, -1);
        autoValidate = typedArray.getBoolean(R.styleable.ZPEditText_zlp_autoValidate, false);
        iconLeftBitmaps = generateIconBitmaps(typedArray.getResourceId(R.styleable.ZPEditText_zlp_iconLeft, -1));
        iconRightBitmaps = generateIconBitmaps(typedArray.getResourceId(R.styleable.ZPEditText_zlp_iconRight, -1));
        showClearButton = typedArray.getBoolean(R.styleable.ZPEditText_zlp_clearButton, false);
        clearButtonBitmaps = generateIconBitmaps(R.drawable.ic_remove_circle);
        iconPadding = typedArray.getDimensionPixelSize(R.styleable.ZPEditText_zlp_iconPadding, getPixel(16));
        floatingLabelAlwaysShown = typedArray.getBoolean(R.styleable.ZPEditText_zlp_floatingLabelAlwaysShown, false);
        helperTextAlwaysShown = typedArray.getBoolean(R.styleable.ZPEditText_zlp_helperTextAlwaysShown, false);
        validateOnFocusLost = typedArray.getBoolean(R.styleable.ZPEditText_zlp_validateOnFocusLost, false);
        checkCharactersCountAtBeginning = typedArray.getBoolean(R.styleable.ZPEditText_zlp_checkCharactersCountAtBeginning, true);
        typedArray.recycle();

        int[] paddings = new int[]{
                android.R.attr.padding, // 0
                android.R.attr.paddingLeft, // 1
                android.R.attr.paddingTop, // 2
                android.R.attr.paddingRight, // 3
                android.R.attr.paddingBottom // 4
        };
        TypedArray paddingsTypedArray = context.obtainStyledAttributes(attrs, paddings);
        int padding = paddingsTypedArray.getDimensionPixelSize(0, 0);
        innerPaddingLeft = paddingsTypedArray.getDimensionPixelSize(1, padding);
        innerPaddingTop = paddingsTypedArray.getDimensionPixelSize(2, padding);
        innerPaddingRight = paddingsTypedArray.getDimensionPixelSize(3, padding);
        innerPaddingBottom = paddingsTypedArray.getDimensionPixelSize(4, padding);
        paddingsTypedArray.recycle();
        setBackground(null);

        if (singleLineEllipsis) {
            TransformationMethod transformationMethod = getTransformationMethod();
            setSingleLine();
            setTransformationMethod(transformationMethod);
        }
        initMinBottomLines();
        initPadding();
        initText();
        initFloatingLabel();
        initTextWatcher();
        checkCharactersCount();
    }

    private void initText() {
        if (!TextUtils.isEmpty(getText())) {
            CharSequence text = getText();
            setText(null);
            resetHintTextColor();
            setText(text);
            setSelection(text.length());
            floatingLabelFraction = 1;
            floatingLabelShown = true;
        } else {
            resetHintTextColor();
        }
        resetTextColor();
    }

    private void initTextWatcher() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkCharactersCount();
                if (autoValidate) {
                    validate();
                } else {
                    setError(null);
                }
                postInvalidate();
            }
        });
    }

    private Typeface getCustomTypeface(@NonNull String fontPath) {
        return Typeface.createFromAsset(getContext().getAssets(), fontPath);
    }

    public void setIconLeft(@DrawableRes int res) {
        iconLeftBitmaps = generateIconBitmaps(res);
        initPadding();
    }

    public void setIconLeft(Drawable drawable) {
        iconLeftBitmaps = generateIconBitmaps(drawable);
        initPadding();
    }

    public void setIconLeft(Bitmap bitmap) {
        iconLeftBitmaps = generateIconBitmaps(bitmap);
        initPadding();
    }

    public void setIconRight(@DrawableRes int res) {
        iconRightBitmaps = generateIconBitmaps(res);
        initPadding();
    }

    public void setIconRight(Drawable drawable) {
        iconRightBitmaps = generateIconBitmaps(drawable);
        initPadding();
    }

    public void setIconRight(Bitmap bitmap) {
        iconRightBitmaps = generateIconBitmaps(bitmap);
        initPadding();
    }

    public boolean isShowClearButton() {
        return showClearButton;
    }

    public void setShowClearButton(boolean show) {
        showClearButton = show;
        correctPaddings();
    }

    private Bitmap[] generateIconBitmaps(@DrawableRes int origin) {
        if (origin == -1) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), origin, options);
        int size = Math.max(options.outWidth, options.outHeight);
        options.inSampleSize = size > iconSize ? size / iconSize : 1;
        options.inJustDecodeBounds = false;
        return generateIconBitmaps(BitmapFactory.decodeResource(getResources(), origin, options));
    }

    private Bitmap[] generateIconBitmaps(Drawable drawable) {
        if (drawable == null)
            return null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return generateIconBitmaps(Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, false));
    }

    private Bitmap[] generateIconBitmaps(Bitmap origin) {
        if (origin == null) {
            return null;
        }
        Bitmap[] iconBitmaps = new Bitmap[4];
        origin = scaleIcon(origin);
        iconBitmaps[0] = origin.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(iconBitmaps[0]);
        canvas.drawColor(baseColor & 0x00ffffff | (Utils.isLight(baseColor) ? 0xff000000 : 0x8a000000), PorterDuff.Mode.SRC_IN);
        iconBitmaps[1] = origin.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(iconBitmaps[1]);
        canvas.drawColor(primaryColor, PorterDuff.Mode.SRC_IN);
        iconBitmaps[2] = origin.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(iconBitmaps[2]);
        canvas.drawColor(baseColor & 0x00ffffff | (Utils.isLight(baseColor) ? 0x4c000000 : 0x42000000), PorterDuff.Mode.SRC_IN);
        iconBitmaps[3] = origin.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(iconBitmaps[3]);
        canvas.drawColor(errorColor, PorterDuff.Mode.SRC_IN);
        return iconBitmaps;
    }

    private Bitmap scaleIcon(Bitmap origin) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        int size = Math.max(width, height);
        if (size == iconSize) {
            return origin;
        } else if (size > iconSize) {
            int scaledWidth;
            int scaledHeight;
            if (width > iconSize) {
                scaledWidth = iconSize;
                scaledHeight = (int) (iconSize * ((float) height / width));
            } else {
                scaledHeight = iconSize;
                scaledWidth = (int) (iconSize * ((float) width / height));
            }
            return Bitmap.createScaledBitmap(origin, scaledWidth, scaledHeight, false);
        } else {
            return origin;
        }
    }

    public float getFloatingLabelFraction() {
        return floatingLabelFraction;
    }

    public void setFloatingLabelFraction(float floatingLabelFraction) {
        this.floatingLabelFraction = floatingLabelFraction;
        invalidate();
    }

    public float getFocusFraction() {
        return focusFraction;
    }

    public void setFocusFraction(float focusFraction) {
        this.focusFraction = focusFraction;
        invalidate();
    }

    public float getCurrentBottomLines() {
        return currentBottomLines;
    }

    public void setCurrentBottomLines(float currentBottomLines) {
        this.currentBottomLines = currentBottomLines;
        initPadding();
    }

    public boolean isFloatingLabelAlwaysShown() {
        return floatingLabelAlwaysShown;
    }

    public void setFloatingLabelAlwaysShown(boolean floatingLabelAlwaysShown) {
        this.floatingLabelAlwaysShown = floatingLabelAlwaysShown;
        invalidate();
    }

    public boolean isHelperTextAlwaysShown() {
        return helperTextAlwaysShown;
    }

    public void setHelperTextAlwaysShown(boolean helperTextAlwaysShown) {
        this.helperTextAlwaysShown = helperTextAlwaysShown;
        invalidate();
    }

    @Nullable
    public Typeface getAccentTypeface() {
        return accentTypeface;
    }

    public void setAccentTypeface(Typeface accentTypeface) {
        this.accentTypeface = accentTypeface;
        this.textPaint.setTypeface(accentTypeface);
        postInvalidate();
    }

    public boolean isHideUnderline() {
        return hideUnderline;
    }

    public void setHideUnderline(boolean hideUnderline) {
        this.hideUnderline = hideUnderline;
        initPadding();
        postInvalidate();
    }

    public int getUnderlineColor() {
        return underlineColor;
    }

    public void setUnderlineColor(int color) {
        this.underlineColor = color;
        postInvalidate();
    }

    public CharSequence getFloatingLabelText() {
        return floatingLabelText;
    }

    public void setFloatingLabelText(@Nullable CharSequence floatingLabelText) {
        this.floatingLabelText = floatingLabelText == null ? getHint() : floatingLabelText;
        postInvalidate();
    }

    public int getFloatingLabelTextSize() {
        return floatingLabelTextSize;
    }

    public void setFloatingLabelTextSize(int size) {
        floatingLabelTextSize = size;
        initPadding();
    }

    public int getFloatingLabelTextColor() {
        return floatingLabelTextColor;
    }

    public void setFloatingLabelTextColor(int color) {
        this.floatingLabelTextColor = color;
        postInvalidate();
    }

    public int getBottomTextSize() {
        return bottomTextSize;
    }

    public void setBottomTextSize(int size) {
        bottomTextSize = size;
        initPadding();
    }

    private int getPixel(int dp) {
        return Utils.dp2px(getContext(), dp);
    }

    private void initPadding() {
        extraPaddingTop = floatingLabelEnabled ? floatingLabelTextSize + floatingLabelPadding : floatingLabelPadding;
        textPaint.setTextSize(bottomTextSize);
        Paint.FontMetrics textMetrics = textPaint.getFontMetrics();
        extraPaddingBottom = (int) ((textMetrics.descent - textMetrics.ascent) * currentBottomLines) + (hideUnderline ? bottomSpacing : bottomSpacing * 2);
        extraPaddingLeft = iconLeftBitmaps == null ? 0 : (iconOuterWidth + iconPadding);
        extraPaddingRight = iconRightBitmaps == null ? 0 : (iconOuterWidth + iconPadding);
        correctPaddings();
    }

    private void initMinBottomLines() {
        boolean extendBottom = minCharacters > 0 || maxCharacters > 0 || singleLineEllipsis || tempErrorText != null || helperText != null;
        currentBottomLines = minBottomLines = minBottomTextLines > 0 ? minBottomTextLines : extendBottom ? 1 : 0;
    }

    @Deprecated
    @Override
    public final void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
    }

    public void setPaddings(int left, int top, int right, int bottom) {
        innerPaddingTop = top;
        innerPaddingBottom = bottom;
        innerPaddingLeft = left;
        innerPaddingRight = right;
        correctPaddings();
    }

    private void correctPaddings() {
        int buttonsWidthLeft = 0, buttonsWidthRight = 0;
        int buttonsWidth = iconOuterWidth * getButtonsCount();
        if (isRTL()) {
            buttonsWidthLeft = buttonsWidth;
        } else {
            buttonsWidthRight = buttonsWidth;
        }
        super.setPadding(innerPaddingLeft + extraPaddingLeft + buttonsWidthLeft, innerPaddingTop + extraPaddingTop, innerPaddingRight + extraPaddingRight + buttonsWidthRight, innerPaddingBottom + extraPaddingBottom);
    }

    private int getButtonsCount() {
        return isShowClearButton() ? 1 : 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!firstShown) {
            firstShown = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            adjustBottomLines();
        }
    }

    private boolean adjustBottomLines() {
        if (getWidth() == 0) {
            return false;
        }
        int destBottomLines;
        textPaint.setTextSize(bottomTextSize);
        if (tempErrorText != null || helperText != null) {
            Layout.Alignment alignment = (getGravity() & Gravity.RIGHT) == Gravity.RIGHT || isRTL() ?
                    Layout.Alignment.ALIGN_OPPOSITE : (getGravity() & Gravity.LEFT) == Gravity.LEFT ?
                    Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_CENTER;
            textLayout = new StaticLayout(tempErrorText != null ? tempErrorText : helperText, textPaint, getWidth() - getBottomTextLeftOffset() - getBottomTextRightOffset() - getPaddingLeft() - getPaddingRight(), alignment, 1.0f, 0.0f, true);
            destBottomLines = Math.max(textLayout.getLineCount(), minBottomTextLines);
        } else {
            destBottomLines = minBottomLines;
        }
        if (bottomLines != destBottomLines) {
            getBottomLinesAnimator(destBottomLines).start();
        }
        bottomLines = destBottomLines;
        return true;
    }

    public int getInnerPaddingTop() {
        return innerPaddingTop;
    }

    public int getInnerPaddingBottom() {
        return innerPaddingBottom;
    }

    public int getInnerPaddingLeft() {
        return innerPaddingLeft;
    }

    public int getInnerPaddingRight() {
        return innerPaddingRight;
    }

    private void initFloatingLabel() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (floatingLabelEnabled) {
                    if (s.length() == 0) {
                        if (floatingLabelShown) {
                            floatingLabelShown = false;
                            getLabelAnimator().reverse();
                        }
                    } else if (!floatingLabelShown) {
                        floatingLabelShown = true;
                        getLabelAnimator().start();
                    }
                }
            }
        });

        innerFocusChangeListener = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (floatingLabelEnabled && highlightFloatingLabel) {
                    if (hasFocus) {
                        getLabelFocusAnimator().start();
                    } else {
                        getLabelFocusAnimator().reverse();
                    }
                }
                if (validateOnFocusLost && !hasFocus) {
                    validate();
                }
                if (outerFocusChangeListener != null) {
                    outerFocusChangeListener.onFocusChange(v, hasFocus);
                }
            }
        };
        super.setOnFocusChangeListener(innerFocusChangeListener);
    }

    public boolean isValidateOnFocusLost() {
        return validateOnFocusLost;
    }

    public void setValidateOnFocusLost(boolean validate) {
        this.validateOnFocusLost = validate;
    }

    public void setBaseColor(int color) {
        if (baseColor != color) {
            baseColor = color;
        }

        initText();

        postInvalidate();
    }

    public void setPrimaryColor(int color) {
        primaryColor = color;
        postInvalidate();
    }

    public void setZPTextColor(int color) {
        textColorStateList = ColorStateList.valueOf(color);
        resetTextColor();
    }

    public void setZpTextColor(ColorStateList colors) {
        textColorStateList = colors;
        resetTextColor();
    }

    private void resetTextColor() {
        if (textColorStateList == null) {
            textColorStateList = new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}, EMPTY_STATE_SET}, new int[]{baseColor & 0x00ffffff | 0xdf000000, baseColor & 0x00ffffff | 0x44000000});
            setTextColor(textColorStateList);
        } else {
            setTextColor(textColorStateList);
        }
    }

    public void setZPHintTextColor(int color) {
        textColorHintStateList = ColorStateList.valueOf(color);
        resetHintTextColor();
    }

    public void setZPHintTextColor(ColorStateList colors) {
        textColorHintStateList = colors;
        resetHintTextColor();
    }

    private void resetHintTextColor() {
        if (textColorHintStateList == null) {
            setHintTextColor(baseColor & 0x00ffffff | 0x44000000);
        } else {
            setHintTextColor(textColorHintStateList);
        }
    }

    private void setFloatingLabelInternal(int mode) {
        switch (mode) {
            case FLOATING_LABEL_NORMAL:
                floatingLabelEnabled = true;
                highlightFloatingLabel = false;
                break;
            case FLOATING_LABEL_HIGHLIGHT:
                floatingLabelEnabled = true;
                highlightFloatingLabel = true;
                break;
            default:
                floatingLabelEnabled = false;
                highlightFloatingLabel = false;
                break;
        }
    }

    public void setFloatingLabel(@FloatingLabelType int mode) {
        setFloatingLabelInternal(mode);
        initPadding();
    }

    public int getFloatingLabelPadding() {
        return floatingLabelPadding;
    }

    public void setFloatingLabelPadding(int padding) {
        floatingLabelPadding = padding;
        postInvalidate();
    }

    public boolean isFloatingLabelAnimating() {
        return floatingLabelAnimating;
    }

    public void setFloatingLabelAnimating(boolean animating) {
        floatingLabelAnimating = animating;
    }

    public void setSingleLineEllipsis() {
        setSingleLineEllipsis(true);
    }

    public void setSingleLineEllipsis(boolean enabled) {
        singleLineEllipsis = enabled;
        initMinBottomLines();
        initPadding();
        postInvalidate();
    }

    public int getMaxCharacters() {
        return maxCharacters;
    }

    public void setMaxCharacters(int max) {
        maxCharacters = max;
        initMinBottomLines();
        initPadding();
        postInvalidate();
    }

    public int getMinCharacters() {
        return minCharacters;
    }

    public void setMinCharacters(int min) {
        minCharacters = min;
        initMinBottomLines();
        initPadding();
        postInvalidate();
    }

    public int getMinBottomTextLines() {
        return minBottomTextLines;
    }

    public void setMinBottomTextLines(int lines) {
        minBottomTextLines = lines;
        initMinBottomLines();
        initPadding();
        postInvalidate();
    }

    public boolean isAutoValidate() {
        return autoValidate;
    }

    public void setAutoValidate(boolean autoValidate) {
        this.autoValidate = autoValidate;
        if (autoValidate) {
            validate();
        }
    }

    public int getErrorColor() {
        return errorColor;
    }

    public void setErrorColor(int color) {
        errorColor = color;
        postInvalidate();
    }

    public void setHelperText(CharSequence helperText) {
        this.helperText = helperText == null ? null : helperText.toString();
        if (adjustBottomLines()) {
            postInvalidate();
        }
    }

    public String getHelperText() {
        return helperText;
    }

    public int getHelperTextColor() {
        return helperTextColor;
    }

    public void setHelperTextColor(int color) {
        helperTextColor = color;
        postInvalidate();
    }

    @Override
    public void setError(CharSequence errorText) {
        tempErrorText = errorText == null ? null : errorText.toString();
        if (adjustBottomLines()) {
            postInvalidate();
        }
    }

    @Override
    public CharSequence getError() {
        return tempErrorText;
    }

    private boolean isInternalValid() {
        return tempErrorText == null && isCharactersCountValid();
    }

    public boolean validateWith(@NonNull ZPEditTextValidate validator) {
        CharSequence text = getText();
        boolean isValid = validator.isValid(text);
        if (!isValid) {
            setError(validator.getErrorMessage());
        }
        postInvalidate();
        return isValid;
    }

    public boolean validate() {
        if (validators == null || validators.isEmpty()) {
            return true;
        }

        CharSequence text = getText();
        boolean isEmpty = text.length() == 0;

        boolean isValid = true;
        for (ZPEditTextValidate validator : validators) {
            //noinspection ConstantConditions
            isValid = isValid && validator.isValid(text);
            if (!isValid) {
                setError(validator.getErrorMessage());
                break;
            }
        }
        if (isValid) {
            setError(null);
        }

        postInvalidate();
        return isValid;
    }

    public boolean hasValidators() {
        return this.validators != null && !this.validators.isEmpty();
    }

    public ZPEditText addValidator(ZPEditTextValidate validator) {
        if (validators == null) {
            this.validators = new ArrayList<>();
        }
        this.validators.add(validator);
        return this;
    }

    public void clearValidators() {
        if (this.validators != null) {
            this.validators.clear();
        }
    }

    @Nullable
    public List<ZPEditTextValidate> getValidators() {
        return this.validators;
    }

    public void setLengthChecker(ZPEditTextLengthChecker lengthChecker) {
        this.lengthChecker = lengthChecker;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        if (innerFocusChangeListener == null) {
            super.setOnFocusChangeListener(listener);
        } else {
            outerFocusChangeListener = listener;
        }
    }

    private ObjectAnimator getLabelAnimator() {
        if (labelAnimator == null) {
            labelAnimator = ObjectAnimator.ofFloat(this, "floatingLabelFraction", 0f, 1f);
        }
        labelAnimator.setDuration(floatingLabelAnimating ? 300 : 0);
        return labelAnimator;
    }

    private ObjectAnimator getLabelFocusAnimator() {
        if (labelFocusAnimator == null) {
            labelFocusAnimator = ObjectAnimator.ofFloat(this, "focusFraction", 0f, 1f);
        }
        return labelFocusAnimator;
    }

    private ObjectAnimator getBottomLinesAnimator(float destBottomLines) {
        if (bottomLinesAnimator == null) {
            bottomLinesAnimator = ObjectAnimator.ofFloat(this, "currentBottomLines", destBottomLines);
        } else {
            bottomLinesAnimator.cancel();
            bottomLinesAnimator.setFloatValues(destBottomLines);
        }
        return bottomLinesAnimator;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        int startX = getScrollX() + (iconLeftBitmaps == null ? 0 : (iconOuterWidth + iconPadding)) + getPaddingLeft();
        int endX = getScrollX() + (iconRightBitmaps == null ? getWidth() : getWidth() - iconOuterWidth - iconPadding) - getPaddingRight();
        int lineStartY = getScrollY() + getHeight() - getPaddingBottom();

        // draw the icon(s)
        paint.setAlpha(255);
        if (iconLeftBitmaps != null) {
            Bitmap icon = iconLeftBitmaps[!isInternalValid() ? 3 : !isEnabled() ? 2 : hasFocus() ? 1 : 0];
            int iconLeft = startX - iconPadding - iconOuterWidth + (iconOuterWidth - icon.getWidth()) / 2;
            int iconTop = lineStartY + bottomSpacing - iconOuterHeight + (iconOuterHeight - icon.getHeight()) / 2;
            canvas.drawBitmap(icon, iconLeft, iconTop, paint);
        }
        if (iconRightBitmaps != null) {
            Bitmap icon = iconRightBitmaps[!isInternalValid() ? 3 : !isEnabled() ? 2 : hasFocus() ? 1 : 0];
            int iconRight = endX + iconPadding + (iconOuterWidth - icon.getWidth()) / 2;
            int iconTop = lineStartY + bottomSpacing - iconOuterHeight + (iconOuterHeight - icon.getHeight()) / 2;
            canvas.drawBitmap(icon, iconRight, iconTop, paint);
        }

        // draw the clear button
        if (hasFocus() && showClearButton && !TextUtils.isEmpty(getText()) && isEnabled()) {
            paint.setAlpha(255);
            int buttonLeft;
            if (isRTL()) {
                buttonLeft = startX;
            } else {
                buttonLeft = endX - iconOuterWidth;
            }
            Bitmap clearButtonBitmap = clearButtonBitmaps[0];
            buttonLeft += (iconOuterWidth - clearButtonBitmap.getWidth()) / 2;
            int iconTop = lineStartY + bottomSpacing - iconOuterHeight + (iconOuterHeight - clearButtonBitmap.getHeight()) / 2;
            canvas.drawBitmap(clearButtonBitmap, buttonLeft, iconTop, paint);
        }

        // draw the underline
        if (!hideUnderline) {
            lineStartY += bottomSpacing;
            if (!isInternalValid()) { // not valid
                paint.setColor(errorColor);
                canvas.drawRect(startX, lineStartY, endX, lineStartY + getPixel(2), paint);
            } else if (!isEnabled()) { // disabled
                paint.setColor(underlineColor != -1 ? underlineColor : baseColor & 0x00ffffff | 0x44000000);
                float interval = getPixel(1);
                for (float xOffset = 0; xOffset < getWidth(); xOffset += interval * 3) {
                    canvas.drawRect(startX + xOffset, lineStartY, startX + xOffset + interval, lineStartY + getPixel(1), paint);
                }
            } else if (hasFocus()) { // focused
                paint.setColor(primaryColor);
                canvas.drawRect(startX, lineStartY, endX, lineStartY + getPixel(2), paint);
            } else { // normal
                paint.setColor(underlineColor != -1 ? underlineColor : baseColor & 0x00ffffff | 0x1E000000);
                canvas.drawRect(startX, lineStartY, endX, lineStartY + getPixel(1), paint);
            }
        }

        textPaint.setTextSize(bottomTextSize);
        Paint.FontMetrics textMetrics = textPaint.getFontMetrics();
        float relativeHeight = -textMetrics.ascent - textMetrics.descent;
        float bottomTextPadding = bottomTextSize + textMetrics.ascent + textMetrics.descent;

        // draw the characters counter
        if ((hasFocus() && hasCharactersCounter()) || !isCharactersCountValid()) {
            textPaint.setColor(isCharactersCountValid() ? (baseColor & 0x00ffffff | 0x44000000) : errorColor);
            String charactersCounterText = getCharactersCounterText();
            canvas.drawText(charactersCounterText, isRTL() ? startX : endX - textPaint.measureText(charactersCounterText), lineStartY + bottomSpacing + relativeHeight, textPaint);
        }

        // draw the bottom text
        if (textLayout != null) {
            if (tempErrorText != null || ((helperTextAlwaysShown || hasFocus()) && !TextUtils.isEmpty(helperText))) { // error text or helper text
                textPaint.setColor(tempErrorText != null ? errorColor : helperTextColor != -1 ? helperTextColor : (baseColor & 0x00ffffff | 0x44000000));
                canvas.save();
                if (isRTL()) {
                    canvas.translate(endX - textLayout.getWidth(), lineStartY + bottomSpacing - bottomTextPadding);
                } else {
                    canvas.translate(startX + getBottomTextLeftOffset(), lineStartY + bottomSpacing - bottomTextPadding);
                }
                textLayout.draw(canvas);
                canvas.restore();
            }
        }

        // draw the floating label
        if (floatingLabelEnabled && !TextUtils.isEmpty(floatingLabelText)) {
            textPaint.setTextSize(floatingLabelTextSize);
            // calculate the text color
            textPaint.setColor((Integer) focusEvaluator.evaluate(focusFraction * (isEnabled() ? 1 : 0), floatingLabelTextColor != -1 ? floatingLabelTextColor : (baseColor & 0x00ffffff | 0x44000000), primaryColor));

            // calculate the horizontal position
            float floatingLabelWidth = textPaint.measureText(floatingLabelText.toString());
            int floatingLabelStartX;
            if ((getGravity() & Gravity.RIGHT) == Gravity.RIGHT || isRTL()) {
                floatingLabelStartX = (int) (endX - floatingLabelWidth);
            } else if ((getGravity() & Gravity.LEFT) == Gravity.LEFT) {
                floatingLabelStartX = startX;
            } else {
                floatingLabelStartX = startX + (int) (getInnerPaddingLeft() + (getWidth() - getInnerPaddingLeft() - getInnerPaddingRight() - floatingLabelWidth) / 2);
            }

            // calculate the vertical position
            int distance = floatingLabelPadding;
            int floatingLabelStartY = (int) (innerPaddingTop + floatingLabelTextSize + floatingLabelPadding - distance * (floatingLabelAlwaysShown ? 1 : floatingLabelFraction) + getScrollY());

            // calculate the alpha
            int alpha = ((int) ((floatingLabelAlwaysShown ? 1 : floatingLabelFraction) * 0xff * (0.74f * focusFraction * (isEnabled() ? 1 : 0) + 0.26f) * (floatingLabelTextColor != -1 ? 1 : Color.alpha(floatingLabelTextColor) / 256f)));
            textPaint.setAlpha(alpha);

            // draw the floating label
            canvas.drawText(floatingLabelText.toString(), floatingLabelStartX, floatingLabelStartY, textPaint);
        }

        // draw the bottom ellipsis
        if (hasFocus() && singleLineEllipsis && getScrollX() != 0) {
            paint.setColor(isInternalValid() ? primaryColor : errorColor);
            float startY = lineStartY + bottomSpacing;
            int ellipsisStartX;
            if (isRTL()) {
                ellipsisStartX = endX;
            } else {
                ellipsisStartX = startX;
            }
            int signum = isRTL() ? -1 : 1;
            canvas.drawCircle(ellipsisStartX + signum * bottomEllipsisSize / 2, startY + bottomEllipsisSize / 2, bottomEllipsisSize / 2, paint);
            canvas.drawCircle(ellipsisStartX + signum * bottomEllipsisSize * 5 / 2, startY + bottomEllipsisSize / 2, bottomEllipsisSize / 2, paint);
            canvas.drawCircle(ellipsisStartX + signum * bottomEllipsisSize * 9 / 2, startY + bottomEllipsisSize / 2, bottomEllipsisSize / 2, paint);
        }

        // draw the original things
        super.onDraw(canvas);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean isRTL() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        }
        Configuration config = getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private int getBottomTextLeftOffset() {
        return isRTL() ? getCharactersCounterWidth() : getBottomEllipsisWidth();
    }

    private int getBottomTextRightOffset() {
        return isRTL() ? getBottomEllipsisWidth() : getCharactersCounterWidth();
    }

    private int getCharactersCounterWidth() {
        return hasCharactersCounter() ? (int) textPaint.measureText(getCharactersCounterText()) : 0;
    }

    private int getBottomEllipsisWidth() {
        return singleLineEllipsis ? (bottomEllipsisSize * 5 + getPixel(4)) : 0;
    }

    private void checkCharactersCount() {
        if ((!firstShown && !checkCharactersCountAtBeginning) || !hasCharactersCounter()) {
            charactersCountValid = true;
        } else {
            CharSequence text = getText();
            int count = text == null ? 0 : checkLength(text);
            charactersCountValid = (count >= minCharacters && (maxCharacters <= 0 || count <= maxCharacters));
        }
    }

    public boolean isCharactersCountValid() {
        return charactersCountValid;
    }

    private boolean hasCharactersCounter() {
        return minCharacters > 0 || maxCharacters > 0;
    }

    private String getCharactersCounterText() {
        String text;
        if (minCharacters <= 0) {
            text = isRTL() ? maxCharacters + " / " + checkLength(getText()) : checkLength(getText()) + " / " + maxCharacters;
        } else if (maxCharacters <= 0) {
            text = isRTL() ? "+" + minCharacters + " / " + checkLength(getText()) : checkLength(getText()) + " / " + minCharacters + "+";
        } else {
            text = isRTL() ? maxCharacters + "-" + minCharacters + " / " + checkLength(getText()) : checkLength(getText()) + " / " + minCharacters + "-" + maxCharacters;
        }
        return text;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (singleLineEllipsis && getScrollX() > 0 && event.getAction() == MotionEvent.ACTION_DOWN && event.getX() < getPixel(4 * 5) && event.getY() > getHeight() - extraPaddingBottom - innerPaddingBottom && event.getY() < getHeight() - innerPaddingBottom) {
            setSelection(0);
            return false;
        }
        if (hasFocus() && showClearButton && isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (insideClearButton(event)) {
                        clearButtonTouched = true;
                        clearButtonClicking = true;
                        return true;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (clearButtonClicking && !insideClearButton(event)) {
                        clearButtonClicking = false;
                    }
                    if (clearButtonTouched) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (clearButtonClicking) {
                        if (!TextUtils.isEmpty(getText())) {
                            setText(null);
                        }
                        clearButtonClicking = false;
                    }
                    if (clearButtonTouched) {
                        clearButtonTouched = false;
                        return true;
                    }
                    clearButtonTouched = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    clearButtonTouched = false;
                    clearButtonClicking = false;
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean insideClearButton(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int startX = getScrollX() + (iconLeftBitmaps == null ? 0 : (iconOuterWidth + iconPadding));
        int endX = getScrollX() + (iconRightBitmaps == null ? getWidth() : getWidth() - iconOuterWidth - iconPadding);
        int buttonLeft;
        if (isRTL()) {
            buttonLeft = startX;
        } else {
            buttonLeft = endX - iconOuterWidth;
        }
        int buttonTop = getScrollY() + getHeight() - getPaddingBottom() + bottomSpacing - iconOuterHeight;
        return (x >= buttonLeft && x < buttonLeft + iconOuterWidth && y >= buttonTop && y < buttonTop + iconOuterHeight);
    }

    private int checkLength(CharSequence text) {
        if (lengthChecker == null) return text.length();
        return lengthChecker.getLength(text);
    }
}
