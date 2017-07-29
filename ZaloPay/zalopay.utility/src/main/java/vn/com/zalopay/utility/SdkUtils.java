package vn.com.zalopay.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SdkUtils {

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

    /**
     * Check device phone or tablet
     *
     * @param context
     * @return
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /***
     * get app version
     * @param pContext
     * @return
     */
    public static String getAppVersion(Context pContext) {
        String versionName = null;
        try {
            versionName = pContext != null ? pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), 0).versionName : null;
        } catch (Exception e) {
        }
        return versionName;
    }

    private static byte[] getHash(String password) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        digest.reset();
        return digest.digest(password.getBytes());
    }

    public static int heightScreen(Activity context)//return height real of screen device
    {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static int widthScreen(Activity context)//return width real of screen device
    {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static String convertDateTime(long timestamp) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = new Date(timestamp);
            return dateFormat.format(date);
        } catch (Exception e) {
        }
        return "";
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int dp2px(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    private static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "x", new BigInteger(1, data));
    }

    public static String sha256(String pPassword) {
        return bin2hex(getHash(pPassword));
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    public static float px2dp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (px / (metrics.densityDpi / 160f));
    }

    public static int getMonth() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static int getYear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        String strYear = String.valueOf(calendar.get(Calendar.YEAR));
        return Integer.parseInt(strYear.substring(2));
    }

    public static void hideSoftKeyboard(Context context, Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
        }
    }

    public static void focusAndSoftKeyboard(Activity pActivity, EditText pEdittext) {
        if (pEdittext == null) {
            return;
        }
        try {
            pEdittext.setFocusable(true);
            pEdittext.setFocusableInTouchMode(true);
            pEdittext.requestFocus();
            pEdittext.setCursorVisible(true);
            InputMethodManager imm = (InputMethodManager) pActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(pEdittext, InputMethodManager.SHOW_IMPLICIT);

        } catch (Exception e) {
        }
    }

    public static String convertDate(long timestamp) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");//yyyy/MM/dd HH:mm:ss
            Date date = new Date(timestamp);
            return dateFormat.format(date);
        } catch (Exception e) {

        }
        return "";
    }

    public static String getStringColor(int pIdColor) {
        String stringColor = "#" + Integer.toHexString(pIdColor);
        stringColor = "#" + stringColor.substring(3, 9);
        return stringColor;
    }

    //LUHN (MOD10) ALGORITHM
    public static boolean validateCardNumberByLuhn(String pCardNumber) {
        pCardNumber = pCardNumber.replace(" ", "");
        int sum = 0;
        int length = pCardNumber.length();
        for (int i = 0; i < pCardNumber.length(); i++) {
            if (0 == (i % 2)) {
                sum += pCardNumber.charAt(length - i - 1) - '0';
            } else {
                sum += sumDigits((pCardNumber.charAt(length - i - 1) - '0') * 2);
            }
        }
        return 0 == (sum % 10);
    }

    private static int sumDigits(int i) {
        return (i % 10) + (i / 10);
    }

    /*
     * check user using the default keyboard
     */
    public static boolean useDefaultKeyBoard(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD).matches(context.getResources().getString(R.string.zpsdk_inputmethod_mane));
    }

    /*
     * Capture screen fail
     */
    public static Bitmap CaptureScreenshot(Activity pActivity) {
        try {
            View rootView = pActivity.getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            Bitmap bitmap;
            if (rootView.getWidth() > 1000) {
                bitmap = Bitmap.createScaledBitmap(rootView.getDrawingCache(), rootView.getWidth() / 2, rootView.getHeight() / 2, true);
            } else {
                bitmap = Bitmap.createScaledBitmap(rootView.getDrawingCache(), rootView.getWidth(), rootView.getHeight(), true);
            }
            rootView.setDrawingCacheEnabled(false);
            return bitmap;
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static void openWebPage(Context pContext, String pUrl) {
        Uri webpage = Uri.parse(pUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(pContext.getPackageManager()) != null) {
            pContext.startActivity(intent);
        }
    }

}
