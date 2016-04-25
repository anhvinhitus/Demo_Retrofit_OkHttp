package vn.com.vng.zalopay.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import vn.com.vng.zalopay.R;

/**
 * Created by longlv on 10/6/15.
 */


public class ToastUtil {
    private static Toast mToast;
//    public static void showToast(int paramInt)
//    {
//        Toast.makeText(MyApplication.instance(), paramInt, Toast.LENGTH_SHORT).show();
//    }

    public static void showToast(Context paramContext, int paramInt) {
        showToast(paramContext, paramInt, 0);
    }

    //  NOTE: cannot change the duration of Toast message
    //  LENGTH_SHORT:2 seconds
    //  LENGTH_LONG: 3.5 seconds
    public static void showToastWithDuration(final Activity activity, final CharSequence paramCharSequence, final int toastDurationInMilliSeconds) {
        if (activity == null || TextUtils.isEmpty(paramCharSequence)) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                final Toast toast = Toast.makeText(activity, null, Toast.LENGTH_LONG);
                LinearLayout localLinearLayout = (LinearLayout) toast.getView();
                toast.setGravity(Gravity.CENTER, 0, 0);
//        localLinearLayout.setBackgroundColor(Color.parseColor("#323049"));
                localLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                localLinearLayout.setGravity(Gravity.CENTER);
                TextView textView = new TextView(activity);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.parseColor("#ffffffff"));
                //int toastSize = (int)paramContext.getResources().getDimension(R.dimen.toast_size);
                textView.setTextSize(16);
                int padding = (int) activity.getResources().getDimension(R.dimen.fab_margin);
                textView.setPadding(padding, padding, padding, padding);
                textView.setText(paramCharSequence);
                localLinearLayout.addView(textView);
                // Set the countdown to display the toast
                final CountDownTimer toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000 /*Tick duration*/) {
                    public void onTick(long millisUntilFinished) {
                        toast.show();
                    }

                    public void onFinish() {
                        toast.cancel();
                    }
                };
                // Show the toast and starts the countdown
                toast.show();
                toastCountDown.start();
            }
        });

    }

    public static void showToast(Context paramContext, int paramInt1, int paramInt2, int gravity) {
        showToast(paramContext, paramContext.getString(paramInt1), paramInt2, gravity);
    }

    public static void showToast(Context paramContext, int paramInt1, int paramInt2) {
        showToast(paramContext, paramContext.getString(paramInt1), paramInt2);
    }

    public static void showToast(final Activity activity, final CharSequence paramCharSequence) {
        if (activity == null)
            return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(activity, paramCharSequence, Toast.LENGTH_LONG);
            }
        });
    }

    public static void showToast(Context paramContext, CharSequence paramCharSequence) {
        showToast(paramContext, paramCharSequence, Toast.LENGTH_LONG);
    }

    public static void showToast(Context paramContext, CharSequence paramCharSequence, int paramInt) {
        if (paramContext == null || TextUtils.isEmpty(paramCharSequence)) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        int dpi = paramContext.getResources().getDisplayMetrics().densityDpi;
        mToast = Toast.makeText(paramContext, null, paramInt);
        LinearLayout localLinearLayout = (LinearLayout) mToast.getView();
        mToast.setGravity(Gravity.CENTER, 0, 0);
//        localLinearLayout.setBackgroundColor(Color.parseColor("#323049"));
        localLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        localLinearLayout.setGravity(Gravity.CENTER);
        TextView textView = new TextView(paramContext);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.parseColor("#ffffffff"));
        //int toastSize = (int)paramContext.getResources().getDimension(R.dimen.toast_size);
        textView.setTextSize(16);
        int padding = (int) paramContext.getResources().getDimension(R.dimen.fab_margin);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(paramCharSequence);
        localLinearLayout.addView(textView);
        mToast.show();
    }

    public static void showToast(Context paramContext, CharSequence paramCharSequence, int paramInt, int gravity) {
        if (paramContext == null || TextUtils.isEmpty(paramCharSequence)) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        int dpi = paramContext.getResources().getDisplayMetrics().densityDpi;
        mToast = Toast.makeText(paramContext, null, paramInt);
        LinearLayout localLinearLayout = (LinearLayout) mToast.getView();

        if (gravity == Gravity.CENTER) {
            mToast.setGravity(gravity, 0, 0);
        } else if (gravity == Gravity.TOP) {
            mToast.setGravity(gravity, 0, -1 * (int) AndroidUtils.dpToPixels(paramContext, 64));
        } else {
            mToast.setGravity(gravity, 0, (int) AndroidUtils.dpToPixels(paramContext, 64));
        }
//        localLinearLayout.setBackgroundColor(Color.parseColor("#323049"));
        localLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        localLinearLayout.setGravity(Gravity.CENTER);
        TextView textView = new TextView(paramContext);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.parseColor("#ffffffff"));
        //int toastSize = (int)paramContext.getResources().getDimension(R.dimen.toast_size);
        textView.setTextSize(16);
        int padding = (int) paramContext.getResources().getDimension(R.dimen.fab_margin);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(paramCharSequence);
        localLinearLayout.addView(textView);
        mToast.show();
    }

//    public static void showToast(CharSequence paramCharSequence)
//    {
//        Toast.makeText(MyApplication.instance(), paramCharSequence, Toast.LENGTH_SHORT).show();
//    }

    public static void showSimpleToast(Context paramContext, CharSequence paramCharSequence, int paramInt, int gravity) {
        if (paramContext == null)
            return;
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        mToast = Toast.makeText(paramContext, paramCharSequence, paramInt);

        if (gravity == Gravity.CENTER) {
            mToast.setGravity(gravity, 0, 0);
        } else if (gravity == Gravity.TOP) {
            mToast.setGravity(gravity, 0, -1 * (int) AndroidUtils.dpToPixels(paramContext, 64));
        } else {
            mToast.setGravity(gravity, 0, (int) AndroidUtils.dpToPixels(paramContext, 64));
        }
        mToast.show();
    }
}