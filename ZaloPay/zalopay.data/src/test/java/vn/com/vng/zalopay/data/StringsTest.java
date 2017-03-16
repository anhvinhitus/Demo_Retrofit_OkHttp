package vn.com.vng.zalopay.data;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.util.Strings;

/**
 * Created by huuhoa on 7/5/16.
 */
public class StringsTest {

    @Test
    public void testStripAccents() throws Exception {
        String input = "abcd";
        String output = "abcd";

        String value = Strings.stripAccents(input);
        Assert.assertTrue(output.equals(value));

        input = "Cộng hoà xã hội chủ nghĩa Việt Nam - Độc lập tự do hạnh phúc";
        output = "Cong hoa xa hoi chu nghia Viet Nam - Doc lap tu do hanh phuc";

        value = Strings.stripAccents(input);
        Assert.assertTrue(output.equals(value));
    }

    @Test
    public void testJoinWithDelimiter() throws Exception {
        List<String> input = new ArrayList<>();
        input.add("1");
        input.add("2");
        input.add("3");
        String expected = "1|2|3";

        String output = Strings.joinWithDelimiter("|", input);
        Assert.assertTrue(expected.equals(output));
    }


    @Test
    public void testJoinLongListWithDelimiter() throws Exception {
        List<Long> input = new ArrayList<>();
        input.add(1L);
        input.add(2L);
        input.add(3L);
        String expected = "1|2|3";

        String output = Strings.joinWithDelimiter("|", input);
        Assert.assertTrue(expected.equals(output));
    }

    @Test
    public void testStripLeadingPath() {
        String input = "../../fonts/zalopay.ttf";
        String expected = "fonts/zalopay.ttf";

        String output = Strings.stripLeadingPath(input);

        Assert.assertEquals(expected, output);
        Assert.assertEquals("", Strings.stripLeadingPath(null));
        Assert.assertEquals("", Strings.stripLeadingPath(""));
        Assert.assertEquals("", Strings.stripLeadingPath(".."));
        Assert.assertEquals("", Strings.stripLeadingPath("../"));
        Assert.assertEquals("", Strings.stripLeadingPath("/.."));
        Assert.assertEquals("", Strings.stripLeadingPath("../.."));
        Assert.assertEquals("", Strings.stripLeadingPath("../../.."));
        Assert.assertEquals("", Strings.stripLeadingPath("/"));
        Assert.assertEquals("", Strings.stripLeadingPath("/////.."));
        Assert.assertEquals("fonts", Strings.stripLeadingPath("fonts"));
        Assert.assertEquals("main.jsbundle", Strings.stripLeadingPath("main.jsbundle"));
        Assert.assertEquals("drawable/abc/../abc.png", Strings.stripLeadingPath("drawable/abc/../abc.png"));
    }

    @Test
    public void testGetDomainName() throws Exception {
        String string1 = "https://zpdemo.github.io/vibrate.html";
        String string2 = "https://facebook.com";
        String string3 = "http://drive.google.com/bla/bla/bla";
        String string4 = "http://www.stackoverflow.com/questions";
        String string5 = "http://www-01.hopperspot.com";
        String string6 = "http://wwwsupernatural-brasil.blogspot.com";
        String string7 = "http://zoyanailpolish.blogspot.com";
        String string8 = "http://ww.socialrating.it";
        String string9 = "http://example.co.uk";
        String string10 = "http://sub1.somesite3.com";
        String string11 = "http://1.2.com";
        String string12 = "http://a.b.c.d";
        String string13 = "http://www.zalopay.com.vn";
        String string14 = "https://bbc.co.uk/vietnamese";
        String string15 = "https://www.google.com";
        String string16 = "https://github.com/segunfamisa/bottom-navigation-demo";
        String string17 = "https://gitlab.zalopay.vn/zalopay-apps/app-ios";
        String string18 = "https://developer.android.com/studio/profile/optimize-ui.html";
        String string19 = "https://x.y.z.com/";
        String string20 = "https://c.d/";

        Assert.assertEquals("github.io", Strings.getDomainName(string1));
        Assert.assertEquals("facebook.com", Strings.getDomainName(string2));
        Assert.assertEquals("google.com", Strings.getDomainName(string3));
        Assert.assertEquals("stackoverflow.com", Strings.getDomainName(string4));
        Assert.assertEquals("hopperspot.com", Strings.getDomainName(string5));
        Assert.assertEquals("blogspot.com", Strings.getDomainName(string6));
        Assert.assertEquals("blogspot.com", Strings.getDomainName(string7));
        Assert.assertEquals("socialrating.it", Strings.getDomainName(string8));
        Assert.assertEquals("example.co.uk", Strings.getDomainName(string9));
        Assert.assertEquals("somesite3.com", Strings.getDomainName(string10));
        Assert.assertEquals("2.com", Strings.getDomainName(string11));
        Assert.assertEquals("c.d", Strings.getDomainName(string12));
        Assert.assertEquals("zalopay.com.vn", Strings.getDomainName(string13));
        Assert.assertEquals("bbc.co.uk", Strings.getDomainName(string14));
        Assert.assertEquals("google.com", Strings.getDomainName(string15));
        Assert.assertEquals("github.com", Strings.getDomainName(string16));
        Assert.assertEquals("zalopay.vn", Strings.getDomainName(string17));
        Assert.assertEquals("android.com", Strings.getDomainName(string18));
        Assert.assertEquals("z.com", Strings.getDomainName(string19));
        Assert.assertEquals("c.d", Strings.getDomainName(string20));
    }

    @Test
    public void testInvalidGetDomainName() throws Exception {
        String invalid1 = "zpdemo.github.io/vibrate.html";
        String invalid2 = "www.qr-code-generator.com";

        Assert.assertEquals("", Strings.getDomainName(invalid1));
        Assert.assertEquals("", Strings.getDomainName(invalid2));
    }

    @Test
    public void testGetIndexOfSearchString() throws Exception {
        String string = "Thông tin tài khoản";
        String search1 = "ông";
        String search2 = "n";
        String search3 = "tin tài";
        String search4 = "m";
        String search5 = "ong";

        Assert.assertEquals(2, Strings.getIndexOfSearchString(string, search1));
        Assert.assertEquals(3, Strings.getIndexOfSearchString(string, search2));
        Assert.assertEquals(6, Strings.getIndexOfSearchString(string, search3));
        Assert.assertEquals(-1, Strings.getIndexOfSearchString(string, search4));
        Assert.assertEquals(2, Strings.getIndexOfSearchString(string, search5));
    }
}