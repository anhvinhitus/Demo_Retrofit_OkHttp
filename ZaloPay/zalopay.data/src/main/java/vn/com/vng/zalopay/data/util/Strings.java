package vn.com.vng.zalopay.data.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public final class Strings {

    private Strings() {
    }

    public static String joinWithDelimiter(String delimiter, List longList) {
        return joinWithDelimiter(delimiter, longList.toArray());
    }

    @SafeVarargs
    public static <T> String joinWithDelimiter(String delimiter, T... values) {
        StringBuilder sb = new StringBuilder();
        String loopDelimiter = "";
        for (T value : values) {
            if (value != null) {
                sb.append(loopDelimiter);
                sb.append(String.valueOf(value));

                loopDelimiter = delimiter;
            }
        }

        return sb.toString();
    }

    public static boolean hasAnyPrefix(String number, String... prefixes) {
        if (number == null) {
            return false;
        }
        for (String prefix : prefixes) {
            if (number.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equalsIgnoreCase(String first, String second) {
        if (first == null) {
            return second == null;
        } else {
            return first.equalsIgnoreCase(second);
        }
    }

    public static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        s = s.replaceAll("Đ", "D");
        s = s.replaceAll("đ", "d");
        return s;
    }

    public static String trim(String src) {
        src = src.trim();
        if (src.length() == 0) {
            return src;
        }
        while (src.startsWith("\n")) {
            src = src.substring(1);
        }
        while (src.endsWith("\n")) {
            src = src.substring(0, src.length() - 1);
        }
        return src;
    }

    public static String stripLeadingPath(String input) {
        if (input == null) {
            return "";
        }

        String pattern = "(^\\.\\.)|(^/+)";
        String prevOutput = input;
        String output = input.replaceAll(pattern, "");
        while (!output.equals(prevOutput)) {
            prevOutput = output;
            output = output.replaceAll(pattern, "");
        }
        return output;
    }

    public static String getDomainName(String url) {
        try {
            final String[] DOMAIN = { "com", "org", "net", "edu", "co", "gov", "asia", };

            URI uri = new URI(url);
            String domain = uri.getHost();

            if(domain != null) {
                String dot = ".";
                String[] parts = domain.split("\\.");

                for (String firstDomain : DOMAIN) {
                    if (parts[parts.length - 2].equals(firstDomain) && parts.length >= 3) {
                        return parts[parts.length - 3] + dot + parts[parts.length - 2] + dot + parts[parts.length - 1];
                    }
                }

                return parts[parts.length - 2] + dot + parts[parts.length - 1];
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "";
        }

        return "";
    }

    public static int getIndexOfSearchString(String text, String searchText) {
        String standardString = text.toLowerCase(Locale.getDefault());
        String standardRemoveAccentString = stripAccents(standardString);

        if (standardRemoveAccentString.contains(searchText)) {
            return standardRemoveAccentString.indexOf(searchText);
        } else if(standardString.contains(searchText)) {
            return standardString.indexOf(searchText);
        }

        return -1;
    }
}
