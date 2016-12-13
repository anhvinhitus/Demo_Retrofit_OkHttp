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
        Assert.assertEquals(true, assertEquals(outputItemList, outputBundle));

        inputItemList.add(inputItem);
        outputBundle = Lists.transform(inputItemList, this::transform);
        Assert.assertEquals(false, assertEquals(outputItemList, outputBundle));

        inputItemList.clear();
        outputBundle = Lists.transform(inputItemList, this::transform);
        Assert.assertEquals(true, outputBundle.size() == 0);

        inputItemList = null;
        outputBundle = Lists.transform(inputItemList, this::transform);
        Assert.assertEquals(true, outputBundle.size() == 0);

        outputBundle = Lists.transform(null, null);
        Assert.assertEquals(true, outputBundle.size() == 0);
    }

    private boolean assertElementEquals(SentBundle b1, SentBundle b2) {
        if (b1 == null && b2 != null) { return false; }
        if (b1 != null && b2 == null) { return false; }

        if(b1.numOfOpenedPakages != b2.numOfOpenedPakages) { return false; }
        if(b1.numOfPackages != b2.numOfPackages) { return false; }
        if(b1.totalLuck != b2.totalLuck) { return false; }
        if(b1.sendZaloPayID != b2.sendZaloPayID) { return false; }
        if(b1.type != b2.type) { return false; }
        if(b1.createTime != b2.createTime) { return false; }
        if(b1.lastOpenTime != b2.lastOpenTime) { return false; }
        if(b1.sendMessage != b2.sendMessage) { return false; }
        if(b1.status != b2.status) { return false; }

        return true;
    }

    private boolean assertEquals(List<SentBundle> list1, List<SentBundle> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            if(!assertElementEquals(list1.get(i), list2.get(i))) {
                return false;
            }
        }

        return true;
    }
}
