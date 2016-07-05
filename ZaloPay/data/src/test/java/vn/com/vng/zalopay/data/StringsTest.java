package vn.com.vng.zalopay.data;

import org.junit.Assert;
import org.junit.Test;

import vn.com.vng.zalopay.data.util.Strings;

import static org.junit.Assert.*;

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

        input = "Cộng hoà xã hội chủ nghĩa Việt Nam";
        output = "Cong hoa xa hoi chu nghia Viet Nam";

        value = Strings.stripAccents(input);
        Assert.assertTrue(output.equals(value));
    }
}