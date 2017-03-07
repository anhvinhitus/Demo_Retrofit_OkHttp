package vn.com.zalopay.wallet.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.listener.onShowDetailOrderListener;
import vn.com.zalopay.wallet.utils.Log;

public class EllipsizingTextView extends TextView {
    private static final String ELLIPSIS = "...";
    private final List<EllipsizeListener> ellipsizeListeners = new ArrayList<EllipsizeListener>();
    private int mOffsetDrawable = 0;
    private boolean isEllipsized;
    private boolean isStale;
    private boolean programmaticChange;
    private String fullText;
    private int maxLines = 1;
    private float lineSpacingMultiplier = 1.0f;
    private float lineAdditionalVerticalPadding = 0.0f;
    private Drawable drawableRight;
    private Rect bounds;
    private int actionX, actionY;
    private onShowDetailOrderListener mShowDetailOrderListener;

    public EllipsizingTextView(Context context) {
        super(context);

        if (drawableRight == null) {
            drawableRight = getDrawable(RS.drawable.ic_info);
            if (drawableRight != null) {
                bounds = drawableRight.getBounds();
                mOffsetDrawable = drawableRight.getIntrinsicWidth();
            }
        }
    }

    public EllipsizingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (drawableRight == null) {
            drawableRight = getDrawable(RS.drawable.ic_info);
            if (drawableRight != null) {
                bounds = drawableRight.getBounds();
                mOffsetDrawable = drawableRight.getIntrinsicWidth();
            }
        }
    }

    public EllipsizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnShowDetailOrderListener(onShowDetailOrderListener pListener) {
        mShowDetailOrderListener = pListener;
    }

    protected Drawable getDrawable(String pIconName) {
        //load bitmap
        try {
            return new BitmapDrawable(getResources(), ResourceManager.getImage(pIconName));

        } catch (Exception ex) {
            Log.e(this, ex);
        }

        return null;
    }

    public void addEllipsizeListener(EllipsizeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        ellipsizeListeners.add(listener);
    }

    public void removeEllipsizeListener(EllipsizeListener listener) {
        ellipsizeListeners.remove(listener);
    }

    public boolean isEllipsized() {
        return isEllipsized;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && drawableRight != null) {
            actionX = (int) event.getX();
            actionY = (int) event.getY();

            int x, y;
            int extraTapArea = 13;

            x = (int) (actionX + extraTapArea);
            y = (int) (actionY - extraTapArea);

            x = getWidth() - x;

            if (x <= 0) {
                x += extraTapArea;
            }

            if (y <= 0)
                y = actionY;

            if (bounds != null && bounds.contains(x, y)) {
                if (mShowDetailOrderListener != null)
                    mShowDetailOrderListener.onShowDetailOrder();
            }
        }
        return super.onTouchEvent(event);
    }

    public int getMaxLines() {
        return maxLines;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        this.maxLines = maxLines;
        isStale = true;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        this.lineAdditionalVerticalPadding = add;
        this.lineSpacingMultiplier = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        if (!programmaticChange) {
            fullText = text.toString();
            isStale = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isStale) {
            super.setEllipsize(null);
            resetText();
        }
        super.onDraw(canvas);
    }

    private void resetText() {
        int maxLines = getMaxLines();
        String workingText = fullText;
        boolean ellipsized = false;
        if (maxLines != -1) {
            Layout layout = createWorkingLayout(workingText);
            if (layout.getLineCount() > maxLines) {
                workingText = fullText.substring(0, layout.getLineEnd(maxLines - 1)).trim();
                while (createWorkingLayout(workingText + ELLIPSIS).getLineCount() > maxLines) {
                    int lastSpace = workingText.lastIndexOf(' ');
                    if (lastSpace == -1) {
                        break;
                    }
                    workingText = workingText.substring(0, lastSpace);
                }
                workingText = workingText + ELLIPSIS;
                ellipsized = true;
            }
        }
        if (!workingText.equals(getText())) {
            programmaticChange = true;
            try {
                setText(workingText);
            } finally {
                programmaticChange = false;
            }
        }
        isStale = false;
        if (ellipsized != isEllipsized) {
            isEllipsized = ellipsized;
            for (EllipsizeListener listener : ellipsizeListeners) {
                listener.ellipsizeStateChanged(ellipsized);
            }

            if (drawableRight != null) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null);
            }
        }
    }

    private Layout createWorkingLayout(String workingText) {
        return new StaticLayout(workingText, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight() - mOffsetDrawable,
                Layout.Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineAdditionalVerticalPadding, false);
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt where) {
        // Ellipsize settings are not respected
    }

    public interface EllipsizeListener {
        void ellipsizeStateChanged(boolean ellipsized);
    }
}

