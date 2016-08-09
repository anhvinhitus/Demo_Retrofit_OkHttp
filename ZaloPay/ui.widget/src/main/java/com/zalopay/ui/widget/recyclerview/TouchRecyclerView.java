package vn.vng.uicomponent.widget.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by trung on 10/03/2016.
 */
public class TouchRecyclerView extends RecyclerView {
    private TouchRecyclerViewListener mListener;
    private long mTouchTimeStamp;
    public TouchRecyclerView(Context context) {
        super(context);
        mTouchTimeStamp = 0;
    }

    public TouchRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchTimeStamp = 0;
    }

    public TouchRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchTimeStamp = 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        Log.d("TouchRecycler", "onInterceptTouchEvent");
        if (mListener != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - mTouchTimeStamp > 1000) {
                mTouchTimeStamp = currentTime;
                mListener.onInterceptTouch();
            }
        }
        return super.onInterceptTouchEvent(e);
    }

    public void setListener(TouchRecyclerViewListener mListener) {
        this.mListener = mListener;
    }
}
