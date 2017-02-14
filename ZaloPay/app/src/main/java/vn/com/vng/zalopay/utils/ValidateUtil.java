package vn.com.vng.zalopay.utils;

import android.text.TextUtils;
import android.util.Log;

import vn.com.vng.zalopay.data.util.PhoneUtil;

/**
 * Created by longlv on 12/9/15.
 */
public class ValidateUtil {
    public static final int VALIDATE_PASSWORD_SUCCEEDED = 0;
    public static final int VALIDATE_PASSWORD_OUT_OF_RANGE = 1;
    public static final int VALIDATE_PASSWORD_ERROR_NULL = 2;
    public static final int VALIDATE_PASSWORD_ERROR_EMPTY = 3;
    public static final int VALIDATE_PASSWORD_ERROR_NOT_MATCH = 4;

    public static boolean isValidLengthZPName(String zaloPayName) {
        return !(zaloPayName.length() > 24 || zaloPayName.length() < 4);
    }

    public static boolean isValidZaloPayName(String zaloPayName) {
        if (TextUtils.isEmpty(zaloPayName)) {
            return true;
        } else {
            return zaloPayName.matches("^[a-zA-Z0-9]*");
        }
    }

    public static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidCMNDOrPassport(String input) {
        if (isCMND(input)) {
            return true;
        }
        return isPassport(input);
    }

    private static boolean isCMND(String str) {
        return isNumeric(str);
    }

    private static boolean isPassport(String passport) {
        if (TextUtils.isEmpty(passport)) {
            return false;
        } else {
            return passport.matches("^[a-zA-Z0-9]*");
        }
    }

    public static boolean isMobileNumber(String input) {
        return PhoneUtil.isMobileNumber(input);
    }

    public static int validatePassword(String pass) {
//        StringBuilder retVal = new StringBuilder();
//        boolean hasUppercase = !pass.equals(pass.toLowerCase());
//        boolean hasLowercase = !pass.equals(pass.toUpperCase());
//        boolean hasNumber = pass.matches(".*\\d.*");
//        boolean noSpecialChar = pass.matches("[a-zA-Z0-9 ]*");
//
//        if (hasUppercase && hasLowercase && hasNumber && !noSpecialChar && pass.length() >= 6 && pass.length() <= 20) {
//            Log.i("ValidateUtil","Password validates");
//            return "success";
//        }

        if (pass.length() < 6 || pass.length() > 20) {
            Log.i("ValidateUtil", pass + " is length < 11");
            return VALIDATE_PASSWORD_OUT_OF_RANGE;
//            retVal.append("Password must be between 6 and 20 characters \n");
        }

//        if (!hasUppercase) {
//            Log.i("ValidateUtil",pass + " <-- needs uppercase");
//            retVal.append("Password needs an upper case \n");
//        }
//
//        if (!hasLowercase) {
//            Log.i("ValidateUtil",pass + " <-- needs lowercase");
//            retVal.append("Password needs a lowercase \n");
//        }
//
//        if (!hasNumber) {
//            Log.i("ValidateUtil",pass + "<-- needs a number");
//            retVal.append("Password needs a number \n");
//        }
//
//        if(noSpecialChar){
//            Log.i("ValidateUtil",pass + "<-- needs a specail character");
//            retVal.append("Password needs a special character i.e. !,@,#, etc.  \n");
//        }

        Log.i("ValidateUtil", "Password validates");
        return VALIDATE_PASSWORD_SUCCEEDED;
    }

    public static int validateNewPassword(String pass1, String pass2) {
        if (pass1 == null || pass2 == null) {
            Log.i("ValidateUtil", "Passwords = null");
            return VALIDATE_PASSWORD_ERROR_NULL;
        }

        if (pass1.length() < 1 || pass2.length() < 1) {
            return VALIDATE_PASSWORD_ERROR_EMPTY;
        }

        if (!pass1.equals(pass2)) {
            Log.i("ValidateUtil", pass1 + " != " + pass2);
            return VALIDATE_PASSWORD_ERROR_NOT_MATCH;
        }

        Log.i("ValidateUtil", pass1 + " = " + pass2);

        return validatePassword(pass1);
    }

    public static boolean isEmailAddress(String input) {
        if (TextUtils.isEmpty(input)) return false;
        else {
            return input.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
        }
    }
}
