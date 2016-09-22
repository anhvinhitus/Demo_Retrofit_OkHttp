/*
package com.zalopay.ui.widget.util;

*/
/**
 * Created by AnhHieu on 10/4/15.
 *//*


import android.text.TextUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class StringUtils {
    private static final String normalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


    public static final Map<Character, Character> sVietNamCharacter = new HashMap<Character, Character>();

    static {
        sVietNamCharacter.put('À', 'A');
        sVietNamCharacter.put('Á', 'A');
        sVietNamCharacter.put('À', 'A');
        sVietNamCharacter.put('Ả', 'A');
        sVietNamCharacter.put('Ã', 'A');
        sVietNamCharacter.put('Ạ', 'A');

        sVietNamCharacter.put('Ă', 'A');
        sVietNamCharacter.put('Ằ', 'A');
        sVietNamCharacter.put('Ắ', 'A');
        sVietNamCharacter.put('Ẳ', 'A');
        sVietNamCharacter.put('Ẵ', 'A');
        sVietNamCharacter.put('Ặ', 'A');

        sVietNamCharacter.put('Â', 'A');
        sVietNamCharacter.put('Ầ', 'A');
        sVietNamCharacter.put('Ấ', 'A');
        sVietNamCharacter.put('Ẩ', 'A');
        sVietNamCharacter.put('Ẫ', 'A');
        sVietNamCharacter.put('Ậ', 'A');

        sVietNamCharacter.put('È', 'E');
        sVietNamCharacter.put('É', 'E');
        sVietNamCharacter.put('Ẽ', 'E');
        sVietNamCharacter.put('Ẻ', 'E');
        sVietNamCharacter.put('Ẹ', 'E');

        sVietNamCharacter.put('Ê', 'E');
        sVietNamCharacter.put('Ề', 'E');
        sVietNamCharacter.put('Ế', 'E');
        sVietNamCharacter.put('Ể', 'E');
        sVietNamCharacter.put('Ễ', 'E');
        sVietNamCharacter.put('Ệ', 'E');

        sVietNamCharacter.put('Ì', 'I');
        sVietNamCharacter.put('Í', 'I');
        sVietNamCharacter.put('Ỉ', 'I');
        sVietNamCharacter.put('Ĩ', 'I');
        sVietNamCharacter.put('Ị', 'I');

        sVietNamCharacter.put('Ò', 'O');
        sVietNamCharacter.put('Ó', 'O');
        sVietNamCharacter.put('Ỏ', 'O');
        sVietNamCharacter.put('Õ', 'O');
        sVietNamCharacter.put('Ọ', 'O');

        sVietNamCharacter.put('Ô', 'O');
        sVietNamCharacter.put('Ồ', 'O');
        sVietNamCharacter.put('Ố', 'O');
        sVietNamCharacter.put('Ổ', 'O');
        sVietNamCharacter.put('Ỗ', 'O');
        sVietNamCharacter.put('Ộ', 'O');

        sVietNamCharacter.put('Ơ', 'O');
        sVietNamCharacter.put('Ờ', 'O');
        sVietNamCharacter.put('Ớ', 'O');
        sVietNamCharacter.put('Ở', 'O');
        sVietNamCharacter.put('Ờ', 'O');
        sVietNamCharacter.put('Ợ', 'O');

        sVietNamCharacter.put('U', 'U');
        sVietNamCharacter.put('Ù', 'U');
        sVietNamCharacter.put('Ú', 'U');
        sVietNamCharacter.put('Ủ', 'U');
        sVietNamCharacter.put('Ũ', 'U');
        sVietNamCharacter.put('Ụ', 'U');

        sVietNamCharacter.put('Ư', 'U');
        sVietNamCharacter.put('Ừ', 'U');
        sVietNamCharacter.put('Ứ', 'U');
        sVietNamCharacter.put('Ử', 'U');
        sVietNamCharacter.put('Ữ', 'U');
        sVietNamCharacter.put('Ự', 'U');

        sVietNamCharacter.put('Ỳ', 'Y');
        sVietNamCharacter.put('Ý', 'Y');
        sVietNamCharacter.put('Ỷ', 'Y');
        sVietNamCharacter.put('Ỹ', 'Y');
        sVietNamCharacter.put('Ỵ', 'Y');

        sVietNamCharacter.put('à', 'a');
        sVietNamCharacter.put('á', 'a');
        sVietNamCharacter.put('ả', 'a');
        sVietNamCharacter.put('ã', 'a');
        sVietNamCharacter.put('ạ', 'a');

        sVietNamCharacter.put('ă', 'a');
        sVietNamCharacter.put('ằ', 'a');
        sVietNamCharacter.put('ắ', 'a');
        sVietNamCharacter.put('ẳ', 'a');
        sVietNamCharacter.put('ẵ', 'a');
        sVietNamCharacter.put('ặ', 'a');

        sVietNamCharacter.put('â', 'a');
        sVietNamCharacter.put('ầ', 'a');
        sVietNamCharacter.put('ấ', 'a');
        sVietNamCharacter.put('ẩ', 'a');
        sVietNamCharacter.put('ẫ', 'a');
        sVietNamCharacter.put('ậ', 'a');

        sVietNamCharacter.put('è', 'e');
        sVietNamCharacter.put('é', 'e');
        sVietNamCharacter.put('ẻ', 'e');
        sVietNamCharacter.put('ẽ', 'e');
        sVietNamCharacter.put('ẹ', 'e');

        sVietNamCharacter.put('ê', 'e');
        sVietNamCharacter.put('ề', 'e');
        sVietNamCharacter.put('ế', 'e');
        sVietNamCharacter.put('ể', 'e');
        sVietNamCharacter.put('ễ', 'e');
        sVietNamCharacter.put('ệ', 'e');

        sVietNamCharacter.put('ì', 'i');
        sVietNamCharacter.put('í', 'i');
        sVietNamCharacter.put('ỉ', 'i');
        sVietNamCharacter.put('ĩ', 'i');
        sVietNamCharacter.put('ị', 'i');

        sVietNamCharacter.put('ò', 'o');
        sVietNamCharacter.put('ó', 'o');
        sVietNamCharacter.put('ỏ', 'o');
        sVietNamCharacter.put('õ', 'o');
        sVietNamCharacter.put('ọ', 'o');

        sVietNamCharacter.put('ô', 'o');
        sVietNamCharacter.put('ồ', 'o');
        sVietNamCharacter.put('ố', 'o');
        sVietNamCharacter.put('ổ', 'o');
        sVietNamCharacter.put('ỗ', 'o');
        sVietNamCharacter.put('ộ', 'o');

        sVietNamCharacter.put('ơ', 'o');
        sVietNamCharacter.put('ờ', 'o');
        sVietNamCharacter.put('ớ', 'o');
        sVietNamCharacter.put('ợ', 'o');
        sVietNamCharacter.put('ỡ', 'o');
        sVietNamCharacter.put('ợ', 'o');

        sVietNamCharacter.put('ù', 'u');
        sVietNamCharacter.put('ú', 'u');
        sVietNamCharacter.put('ủ', 'u');
        sVietNamCharacter.put('ũ', 'u');
        sVietNamCharacter.put('ụ', 'u');

        sVietNamCharacter.put('ư', 'u');
        sVietNamCharacter.put('ừ', 'u');
        sVietNamCharacter.put('ứ', 'u');
        sVietNamCharacter.put('ử', 'u');
        sVietNamCharacter.put('ữ', 'u');
        sVietNamCharacter.put('ự', 'u');

        sVietNamCharacter.put('ỳ', 'y');
        sVietNamCharacter.put('ý', 'y');
        sVietNamCharacter.put('ỷ', 'y');
        sVietNamCharacter.put('ỹ', 'y');
        sVietNamCharacter.put('ỵ', 'y');

        sVietNamCharacter.put('đ', 'd');
        sVietNamCharacter.put('Đ', 'd');
    }

    public static String getLastName(String name) {
        String last = "";

        if (name == null)
            return last;

        int id = name.lastIndexOf(" ");

        last = name.substring(id + 1);

        return last;
    }

    public static String removeSpace(String name) {
        String re = "";
        if (name == null)
            return re;

        return name.replace(" ", "");
    }

    public static String getFirstChar(String name) {
        String re = "";
        if (name == null || name.equals(""))
            return re;

        String c = String.valueOf(name.charAt(0));

        re = StringUtils.fromVietnamToVN(c).toUpperCase();

        if (!normalChars.contains(re)) {
            re = "#";
        }

        return re;
    }

    public static String getFirstCharNoCase(String name) {
        String re = "";
        if (name == null || name.equals(""))
            return re;

        String c = String.valueOf(name.charAt(0));

        re = StringUtils.fromVietnamToVN(c);

        if (!normalChars.contains(re)) {
            re = "#";
        }

        return re;
    }

    public static int compareString(String name1, String name2) {
        name1 = removeSpace(name1);
        name2 = removeSpace(name2);

        String temp1 = "";
        String first1 = getFirstCharNoCase(name1);
        if (name1.length() > 1) {
            temp1 = first1.concat(StringUtils.fromVietnamToVN(name1.substring(1)));
        } else {
            temp1 = first1;
        }

        String temp2 = "";
        String first2 = getFirstCharNoCase(name2);
        if (name2.length() > 1) {
            temp2 = first2.concat(StringUtils.fromVietnamToVN(name2.substring(1)));
        } else {
            temp2 = first2;
        }

        // System.out.println("---------->>>>>>>>>>> Compare: " + name1 + " : "
        // + name2 + " : " + first1 + " : " + first2);

        if (first1.equals("#") && !first2.equals("#")) {
            return 1;
        }

        if (!first1.equals("#") && first2.equals("#")) {
            return -1;
        }

        int result = temp1.compareToIgnoreCase(temp2);
        // System.out.println("---------->>>>>>>>>>> Compare: " + temp1 + " : "
        // + temp2 + " : " + result);
        return result;
    }

    public static HashSet<String> stringToHashSet(String strList) {
        HashSet<String> result = new HashSet<String>();
        if (TextUtils.isEmpty(strList))
            return result;
        String[] strArr = strList.split(",");
        for (String aStrArr : strArr) {
            String str = aStrArr.trim();
            if (TextUtils.isEmpty(str))
                continue;
            result.add(str);
        }
        return result;
    }

    public static String hashSetStringToString(HashSet<String> values) {
        StringBuilder result = new StringBuilder();
        Iterator<String> it = values.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (TextUtils.isEmpty(str)) continue;

            result.append(result.length() == 0 ? str : "," + str);
        }
        return result.toString();
    }

    public static String hashSetLongToString(HashSet<Long> values) {
        StringBuilder result = new StringBuilder();

        Iterator<Long> it = values.iterator();
        while (it.hasNext()) {
            Long str = it.next();
            if (str <= 0) continue;

            result.append(result.length() == 0 ? str : "," + str);
        }
        return result.toString();
    }

    public static String removeColor(String messageText) {
        try {
            messageText = messageText.trim();
            if (messageText.length() > 8 && messageText.startsWith("#")
                    && messageText.startsWith(" ", 7)) {
                messageText = messageText.substring(8);
            }
            return messageText;
        } catch (Exception ex) {

        }
        return messageText;
    }


    public static String diacriticsInVietnameseLowerCase(String word) {
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < word.trim().length(); i++) {
            char c = word.charAt(i);
            if (sVietNamCharacter.containsKey(c)) {
                c = sVietNamCharacter.get(c);
            }
            newWord.append(c);
        }
        return newWord.toString().toLowerCase();
    }

    public static String fromTitleToAlias(String word) {
        if (TextUtils.isEmpty(word))
            return "";
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < word.trim().length(); i++) {
            char c = word.charAt(i);
            if (sVietNamCharacter.containsKey(c)) {
                c = sVietNamCharacter.get(c);
            }
            newWord.append(c);
        }
        return newWord.toString().toLowerCase().replaceAll(" ", "-");
    }

    public static String fromVietnamToVN(String content) {
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (sVietNamCharacter.containsKey(c)) {
                c = sVietNamCharacter.get(c);
            }
            newWord.append(c);
        }
        return newWord.toString();
    }
}
*/
