package vn.com.vng.zalopay.data;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
}