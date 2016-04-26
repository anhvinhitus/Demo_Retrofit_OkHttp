package vn.vng.uicomponent.widget.edittext;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;

import vn.vng.uicomponent.widget.R;

/**
 * Created by longlv on 26/04/2016.
 */
public class ClearableEditText extends AppCompatEditText {
    private static final String TAG = "ClearableTextView";

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ClearableEditText(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }
//	private void hideKeyboard() {
//		if (getContext() == null)
//			return;
////		if (view == null)
////			return;
//		InputMethodManager manager = (InputMethodManager) getContext()
//				.getSystemService(Context.INPUT_METHOD_SERVICE);
//		manager.hideSoftInputFromWindow(this.getWindowToken(), 0);
//	}

    //support clear icon
    private Drawable dRight;
    private Rect rBounds;
    private boolean mClearable;
    @Override
    public void setCompoundDrawables(Drawable left, Drawable top,
                                     Drawable right, Drawable bottom) {
        // TODO Auto-generated method stub
//		if (right != null) {
        dRight = right;
//		}
//		dRight.setVisible(isEnabled(), true);
        super.setCompoundDrawables(left, top, right, bottom);
    }

    public boolean isClearable(){
        return mClearable;
    }
    public void setClearable(boolean clearable){
        mClearable = clearable;
        Drawable[] compoundDrawables = getCompoundDrawables();
        if (mClearable){
            setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], compoundDrawables[1],
                    getContext().getResources().getDrawable(R.drawable.ic_remove), compoundDrawables[3]);
        } else {
            setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], null, compoundDrawables[3]);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
//		DebugUtils.d(TAG, "onTouchEvent is called");
        if (event.getAction() == MotionEvent.ACTION_UP && dRight != null && mClearable) {
            rBounds = dRight.getBounds();
            final int x = (int) event.getX() + this.getLeft();
            final int y = (int) event.getY();
//			 System.out.println("x:/y: "+x+"/"+y);
//			 System.out.println("bounds: "+
//				 (this.getRight() - rBounds.width())+"/"+
//				 (this.getRight() - this.getPaddingRight())+"/"+
//				 this.getPaddingTop()+"/"+
//				 (this.getHeight() - this.getPaddingBottom()));
            // check to make sure the touch event was within the bounds of the
            // drawable
//			DebugUtils.d(TAG, "dRight is here");
            if (x >= (this.getRight() - this.getPaddingRight() - rBounds.width())
                    && x <= (this.getRight() - this.getPaddingRight())
                    && y >= this.getPaddingTop()
                    && y <= (this.getHeight() - this.getPaddingBottom())) {
                // System.out.println("touch");
                this.setText("");
				/* use this to prevent the keyboard from coming up */
                event.setAction(MotionEvent.ACTION_CANCEL);
//				DebugUtils.d(TAG, "setText is called");
            }

        }
        return super.onTouchEvent(event);
    }

    public void autoSetText(CharSequence text) {
        // TODO Auto-generated method stub
        super.setText(text);
        setSelection(getText().length());
    }

}

