package vn.com.vng.zalopay.regex;


import android.net.Uri;
import android.text.TextUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vn.com.vng.zalopay.ApplicationTestCase;
import vn.com.vng.zalopay.data.util.ConfigLoader;

/**
 * Created by Duke on 7/12/17.
 */

public class WebAppAllowUrl extends ApplicationTestCase {
    private List<String> allowUrls;
    private String url;

    @Before
    public void init() {
        allowUrls = ConfigLoader.getAllowUrls();
        url = "https://zalopay.com.vn/qrcode/product/";
    }

    @Test
    public void testRegex() {
        Assert.assertTrue(checkAllowUrls(url));
    }

    private boolean checkAllowUrls(String url) {
        boolean isMatched = false;

        if (TextUtils.isEmpty(url)) {
            return false;
        }

        Uri uri = Uri.parse(url);

        if (uri == null || TextUtils.isEmpty(uri.getHost()) || !"https".equals(uri.getScheme())) {
            return false;
        }

        String path = uri.getHost();

        String regex = TextUtils.join("|", allowUrls);
        System.out.println(String.format("Regex: %s", regex));
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);

        while (matcher.find()) {
            System.out.println(String.format("URL: %s", url));
            System.out.println(String.format("Host: %s", path));
            System.out.println(String.format("Full match: %s", matcher.group(0)));
            for (int i = 1; i <= matcher.groupCount(); i++) {
                System.out.println(String.format("Group [%s]: %s", i, matcher.group(i)));
            }
            isMatched = true;
        }

        return isMatched;
    }
}
