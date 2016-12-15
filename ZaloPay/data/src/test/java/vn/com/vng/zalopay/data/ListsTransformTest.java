package vn.com.vng.zalopay.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;

/**
 * Created by cpu11759-local on 12/12/2016.
 */

@RunWith(CustomRobolectricRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class ListsTransformTest {

    private SentBundleGD inputItem;
    private SentBundle bundle;

    private List<SentBundleGD> inputItemList;
    private List<SentBundle> outputItemList;

    @Before
    public void setUp() throws Exception {
        inputItemList = new ArrayList<>();
        outputItemList = new ArrayList<>();
    }

    private SentBundle transform(SentBundleGD inputItem) {
        SentBundle bundle = null;
        if (inputItem != null) {
            bundle = new SentBundle();
            bundle.numOfOpenedPakages = inputItem.numOfOpenedPakages;
            bundle.numOfPackages = inputItem.numOfPackages;
            bundle.totalLuck = inputItem.totalLuck;
            bundle.sendZaloPayID = inputItem.senderZaloPayID;
            bundle.type = inputItem.type;
            bundle.createTime = inputItem.createTime;
            bundle.lastOpenTime = inputItem.lastOpenTime;
            bundle.sendMessage = inputItem.sendMessage;
            bundle.status = inputItem.status;
        }
        return bundle;
    }

    private void initData() {
        inputItem = new SentBundleGD();
        inputItem.numOfOpenedPakages = 10L;
        inputItem.numOfPackages = 20L;
        inputItem.totalLuck = 4L;
        inputItem.senderZaloPayID = "1611100000";
        inputItem.type = 1L;
        inputItem.createTime = 1L;
        inputItem.lastOpenTime = 2L;
        inputItem.sendMessage = "message";
        inputItem.status = 1L;

        inputItemList.add(inputItem);
        inputItemList.add(inputItem);

        bundle = new SentBundle();
        bundle.numOfOpenedPakages = 10;
        bundle.numOfPackages = 20;
        bundle.totalLuck = 4;
        bundle.sendZaloPayID = "1611100000";
        bundle.type = 1;
        bundle.createTime = 1L;
        bundle.lastOpenTime = 2L;
        bundle.sendMessage = "message";
        bundle.status = 1;

        outputItemList.add(bundle);
        outputItemList.add(bundle);
    }

    @Test
    public void transform() {
        initData();

        List<SentBundle> outputBundle = Lists.transform(inputItemList, this::transform);
        assertEquals(outputItemList, outputBundle);

        inputItemList.clear();
        outputBundle = Lists.transform(inputItemList, this::transform);
        Assert.assertEquals("transform list doesn't have data", 0, outputBundle.size());

        inputItemList = null;
        outputBundle = Lists.transform(inputItemList, this::transform);
        Assert.assertEquals("transform null list", 0, outputBundle.size());

        outputBundle = Lists.transform(null, null);
        Assert.assertEquals("transform with two params is null", 0, outputBundle.size());
    }

    private void assertElementEquals(SentBundle b1, SentBundle b2) {
        if (b1 == null && b2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("numOfOpenedPakages", b1.numOfOpenedPakages, b2.numOfOpenedPakages);
        Assert.assertEquals("numOfPackages", b1.numOfPackages, b2.numOfPackages);
        Assert.assertEquals("totalLuck", b1.totalLuck, b2.totalLuck);
        Assert.assertEquals("sendZaloPayID", b1.sendZaloPayID, b2.sendZaloPayID);
        Assert.assertEquals("type", b1.type, b2.type);
        Assert.assertEquals("createTime", b1.createTime, b2.createTime);
        Assert.assertEquals("lastOpenTime", b1.lastOpenTime, b2.lastOpenTime);
        Assert.assertEquals("sendMessage", b1.sendMessage, b2.sendMessage);
        Assert.assertEquals("status", b1.status, b2.status);
    }

    private void assertEquals(List<SentBundle> list1, List<SentBundle> list2) {
        if (list1.size() != list2.size()) {
            Assert.fail("Size doesn't equal");
            return;
        }

        for (int i = 0; i < list1.size(); i++) {
            assertElementEquals(list1.get(i), list2.get(i));
        }
    }
}
