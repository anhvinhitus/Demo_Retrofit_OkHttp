package vn.com.vng.zalopay;

import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import vn.com.vng.zalopay.webapp.WebAppBottomSheetPresenter;

/**
 * Created by khattn on 2/28/17.
 */

public class WebAppBottomSheetPresenterTest {
    private WebAppBottomSheetPresenter mPresenter;

    @Before
    public void setUp() throws Exception {
        Context context = null;
        mPresenter = new WebAppBottomSheetPresenter(context);
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

        Assert.assertEquals("github.io", mPresenter.getDomainName(string1));
        Assert.assertEquals("facebook.com", mPresenter.getDomainName(string2));
        Assert.assertEquals("google.com", mPresenter.getDomainName(string3));
        Assert.assertEquals("stackoverflow.com", mPresenter.getDomainName(string4));
        Assert.assertEquals("hopperspot.com", mPresenter.getDomainName(string5));
        Assert.assertEquals("blogspot.com", mPresenter.getDomainName(string6));
        Assert.assertEquals("blogspot.com", mPresenter.getDomainName(string7));
        Assert.assertEquals("socialrating.it", mPresenter.getDomainName(string8));
        Assert.assertEquals("co.uk", mPresenter.getDomainName(string9));
        Assert.assertEquals("somesite3.com", mPresenter.getDomainName(string10));
        Assert.assertEquals("2.com", mPresenter.getDomainName(string11));
        Assert.assertEquals("c.d", mPresenter.getDomainName(string12));
    }

    @Test
    public void testInvalidGetDomainName() throws Exception {
        String invalid1 = "zpdemo.github.io/vibrate.html";
        String invalid2 = "www.qr-code-generator.com";

        Assert.assertEquals("", mPresenter.getDomainName(invalid1));
        Assert.assertEquals("", mPresenter.getDomainName(invalid2));
    }
}
