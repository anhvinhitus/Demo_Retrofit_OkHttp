package vn.com.zalopay.wallet.business.validation;

import java.util.regex.Pattern;

public class CardValidation {
    //card name mush be character and space
    private static final Pattern mPatternCardName = Pattern.compile("[a-z0-9\\s]+", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    public static boolean validCardName(String pCardName) {
        boolean isValid = mPatternCardName.matcher(pCardName).matches();
        return isValid;
    }
}
