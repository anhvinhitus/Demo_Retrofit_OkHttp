package vn.com.zalopay.wallet.business.webview.base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentHtmlParser {
    public static String getContent(String pHtml) {
        String[] tags = new String[2];
        tags[0] = "div";
        tags[1] = "p";

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < tags.length; i++) {
            Pattern pattern = Pattern.compile("</?([A-Za-z][^\\s>/]*)(?:=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s>]+)|[^>])*(?:>|$)");
            Matcher matcher = pattern.matcher(pHtml);
            while (matcher.find()) {
                String codeGroup = matcher.group(1);
                buffer.append(codeGroup);
            }
        }

        return buffer.toString();
    }
}
