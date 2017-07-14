package vn.com.vng.zalopay.data.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by hieuvm on 7/13/17.
 * *
 */
public class ListsTest {
    @Test
    public void transform() throws Exception {

    }

    @Test
    public void chopped() throws Exception {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        List<List<Integer>> result = Lists.chopped(numbers, 2);
        Assert.assertTrue(result.size() == 4);
        System.out.println(result.toString());

        result = Lists.chopped(numbers, 3);
        Assert.assertTrue(result.size() == 3);
        System.out.println(result.toString());

        result = Lists.chopped(numbers, 4);
        Assert.assertTrue(result.size() == 2);
        System.out.println(result.toString());

        result = Lists.chopped(numbers, 5);
        Assert.assertTrue(result.size() == 2);
        System.out.println(result.toString());

        result = Lists.chopped(numbers, 8);
        Assert.assertTrue(result.size() == 1);
        System.out.println(result.toString());

        result = Lists.chopped(numbers, 9);
        Assert.assertTrue(result.size() == 1);
        System.out.println(result.toString());
    }

    @Test
    public void choppedCheckElementInteger() throws Exception {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

        List<List<Integer>> result = Lists.chopped(numbers, 4);
        Assert.assertTrue(result.get(0).toString().equals(numbers.subList(0, 4).toString()));
        Assert.assertTrue(result.get(1).toString().equals(numbers.subList(4, 8).toString()));

        result = Lists.chopped(numbers, 3);
        Assert.assertTrue(result.get(0).toString().equals(numbers.subList(0, 3).toString()));
        Assert.assertTrue(result.get(1).toString().equals(numbers.subList(3, 6).toString()));
        Assert.assertTrue(result.get(2).toString().equals(numbers.subList(6, 8).toString()));

        result = Lists.chopped(numbers, 2);
        Assert.assertTrue(result.get(0).toString().equals(numbers.subList(0, 2).toString()));
        Assert.assertTrue(result.get(1).toString().equals(numbers.subList(2, 4).toString()));
        Assert.assertTrue(result.get(2).toString().equals(numbers.subList(4, 6).toString()));
        Assert.assertTrue(result.get(3).toString().equals(numbers.subList(6, 8).toString()));
    }

}