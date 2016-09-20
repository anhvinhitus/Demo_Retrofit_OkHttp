package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.zalopay.wallet.business.behavior.factory.AdapterBase;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicEditText;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

/**
 * Created by longlv on 23/05/2016.
 * EditText with clear button at the end.
 */
public class ClearableEditText extends TextInputEditText {
    private static final String TAG = ClearableEditText.class.getName();
    public static final char VERTICAL_SPACE = '-';
    public static final char SLASH_SPACE = '/';
    private char SPACE_SEPERATOR = 45;
    private boolean showClearable = true;
    private DDynamicEditText mEditTextConfig = null;
    private AdapterBase mAdapter = null;
    private boolean mIsTextGroup = false;
    private boolean mIsPattern = false;
    private String mPattern = null;
    private Drawable drawableRightScan;
    private Drawable drawableRightDelete;
    private Rect bounds;
    private int actionX;
    private int actionY;
    private boolean mIsCameraScan = false;
    private View.OnClickListener mSelectedCardScanListener = null;
    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
//            if (!hasFocus) {
//                ClearableEditText.this.checkPattern();
//            }

        }
    };
    private TextWatcher mTextFormater = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ClearableEditText.this.checkEnableDrawableRight();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            ClearableEditText.this.formatText(false);
        }
    };

    public void setOnSelectedCardScanListener(View.OnClickListener listener) {
        this.mSelectedCardScanListener = listener;
    }

    public void setIsCameraScan(boolean value) {
        this.mIsCameraScan = value;
        this.checkEnableDrawableRight();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 0) {
            this.actionX = (int) event.getX();
            this.actionY = (int) event.getY();
            byte extraTapArea = 13;
            int x = this.actionX + extraTapArea;
            int y = this.actionY - extraTapArea;
            x = this.getWidth() - x;
            if (x <= 0) {
                x += extraTapArea;
            }

            if (y <= 0) {
                y = this.actionY;
            }

            if (this.bounds != null && this.bounds.contains(x, y)) {
                Log.e("onTouchEvent", "bounds.contains");
                if (!TextUtils.isEmpty(this.getString())) {
                    this.setText("");
                } else if (this.mSelectedCardScanListener != null) {
                    this.mSelectedCardScanListener.onClick(this);
                }
            }
        }

        return super.onTouchEvent(event);
    }

    public void setShowClearable(boolean showClearable) {
        this.showClearable = showClearable;
    }

    protected void finalize() throws Throwable {
        this.drawableRightScan = null;
        this.drawableRightDelete = null;
        super.finalize();
    }

    public ClearableEditText(Context context) {
        super(context, null);
        this.init(null, 0);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(attrs, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.drawableRightDelete = context.getDrawable(R.drawable.ic_remove_circle);
        } else {
            this.drawableRightDelete = context.getResources().getDrawable(R.drawable.ic_remove_circle);
        }
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        if (this.getContext() instanceof PaymentChannelActivity) {
            this.mAdapter = ((PaymentChannelActivity) this.getContext()).getAdapter();
        }

        this.addTextChangedListener(this.mTextFormater);
        this.setClickable(true);
        this.setEnabled(true);
    }

    public void init(DDynamicEditText pEditText, AdapterBase pAdapter) {
        this.mIsPattern = pEditText.pattern;
        this.mEditTextConfig = pEditText;
        this.mAdapter = pAdapter;
        if (this.mIsPattern) {
            this.setOnFocusChangeListener(this.mOnFocusChangeListener);
        }

        this.addTextChangedListener(this.mTextFormater);
    }

    public boolean isValid() {
        if (this.mIsPattern && this.mAdapter != null) {
            this.mPattern = ResourceManager.getInstance(null).getPattern(this.mEditTextConfig.id, this.mAdapter.getChannelID());
            if (this.mPattern == null) {
                this.mPattern = ResourceManager.getInstance(null).getPattern(this.mEditTextConfig.id, "all");
            }

            if (this.getText().length() == 0) {
                return true;
            } else {
                if (this.mPattern != null) {
                    String text = this.getString();
                    if (text.matches(this.mPattern)) {
                        Timber.d("**** " + this.mEditTextConfig.id + " MATCH ****");
                        return true;
                    }

                    Timber.d("**** " + this.mEditTextConfig.id + " NOT MATCH ****");
                }

                return false;
            }
        } else {
            return true;
        }
    }

//    public boolean checkPattern() {
//        if (!this.isValid()) {
//            DialogManagerZaloPay.showSweetDialogCustom(GlobalData.getOwnerActivity(), this.mEditTextConfig.errMess, GlobalData.getStringResource("dialog_retry_button"), 3, (DialogManagerZaloPay.EventDialog) null);
//            return false;
//        } else {
//            return true;
//        }
//    }

    public void formatText(Boolean isTextFull) {
        Editable s = this.getEditableText();
        if (!this.mIsTextGroup) {
            return;
        }

        char c;
        if (s.length() > 0 && s.length() % 5 == 0) {
            c = s.charAt(s.length() - 1);
            if (this.SPACE_SEPERATOR == c) {
                s.delete(s.length() - 1, s.length());
            }
        }

        if (isTextFull) {
            for (int var6 = 0; var6 < s.length(); ++var6) {
                if (var6 > 1 && var6 % 5 == 0) {
                    filterTextAt(s, var6);
                }
            }

            return;
        }

        if (s.length() > 0 && s.length() % 5 == 0) {
            filterTextAt(s, s.length());
        }
    }

    private void filterTextAt(Editable text, int index) {
        char charAt = text.charAt(index - 1);
        if (Character.isDigit(charAt) && TextUtils.split(text.toString(), String.valueOf(this.SPACE_SEPERATOR)).length <= 3) {
            InputFilter[] textFilters = text.getFilters();
            text.setFilters(new InputFilter[0]);
            text.insert(index - 1, String.valueOf(this.SPACE_SEPERATOR));
            text.setFilters(textFilters);
        }
    }

    public void setGroupText(boolean pIsEnabled) {
        this.mIsTextGroup = pIsEnabled;
    }

    private void checkEnableDrawableRight() {
        if (!showClearable) {
            return;
        }
        String s = this.getString();
        this.setCompoundDrawablePadding(10);
        Bitmap mBitmap;
        if (s.length() > 0) {
            if (this.drawableRightDelete == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.drawableRightDelete = getContext().getDrawable(R.drawable.ic_remove_circle);
                } else {
                    this.drawableRightDelete = this.getResources().getDrawable(R.drawable.ic_remove_circle);
                }
            }

            if (this.drawableRightDelete != null) {
                this.bounds = this.drawableRightDelete.getBounds();
                this.setCompoundDrawablesWithIntrinsicBounds(null, null, this.drawableRightDelete, null);
            }
        } else {
            this.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

    }

    public String getString() {
        if (!this.mIsTextGroup || TextUtils.isEmpty(this.getText().toString())) {
            return this.getText().toString();
        } else {
            return this.getText().toString().replace(String.valueOf(this.SPACE_SEPERATOR), "");
        }
    }

    public static String optText(ClearableEditText editText) {
        if (editText == null) {
            return null;
        }

        Editable text = editText.getText();
        if (text == null) {
            return null;
        }

        return text.toString();
    }
}
