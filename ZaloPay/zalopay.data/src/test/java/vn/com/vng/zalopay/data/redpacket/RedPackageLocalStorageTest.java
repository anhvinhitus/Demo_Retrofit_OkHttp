package vn.com.vng.zalopay.data.redpacket;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.CustomObserver;
import vn.com.vng.zalopay.data.DefaultObserver;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.cache.model.BundleGD;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePacketSummaryDB;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDB;
import vn.com.vng.zalopay.domain.model.redpacket.AppConfigEntity;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class RedPackageLocalStorageTest extends ApplicationTestCase {

    private RedPacketStore.LocalStorage mLocalStorage;
    RedPacketDataMapper mMapper;

    private final int TRANSACTION_SIZE = 20;

    @Before
    public void setup() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mMapper = new RedPacketDataMapper();
        mLocalStorage = new RedPacketLocalStorage(daoSession, mMapper);
    }

    @Test
    public void putBundleEmptyList() {
        List<BundleGD> bundles = new ArrayList<>();

        assertEquals("put empty list", null, mLocalStorage.getBundle(1));
    }

    @Test
    public void putBundle() {
        List<BundleGD> bundles = new ArrayList<>();

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            int j = i + 1;

            BundleGD bundle = new BundleGD();
            bundle.id = j;
            bundle.createTime = 100L;
            bundle.lastTimeGetPackage = 150L;

            bundles.add(bundle);
        }
        mLocalStorage.putBundle(bundles);

        for(int i = 0; i < TRANSACTION_SIZE; i++) {
            compare2Elements(bundles.get(i), mLocalStorage.getBundle(i + 1));
        }
    }

    @Test
    public void updateLastTimeGetPackageWithDBEmpty() {
        try {
            mLocalStorage.updateLastTimeGetPackage(0);
        } catch (Exception e) {
            fail("updateLastTimeGetPackage when DB hasn't datas got error: " + e);
        }
    }

    @Test
    public void updateLastTimeGetPackage() {
        List<BundleGD> bundles = new ArrayList<>();

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            int j = i + 1;

            BundleGD bundle = new BundleGD();
            bundle.id = j;
            bundle.createTime = 100L;
            bundle.lastTimeGetPackage = 150L;

            bundles.add(bundle);
        }
        mLocalStorage.putBundle(bundles);

        try {
            mLocalStorage.updateLastTimeGetPackage(1);
        } catch (Exception e) {
            fail("updateLastTimeGetPackage got error: " + e);
        }
    }

    @Test
    public void getBundleWithEmptyDB() {
        assertEquals("getBundle with empty DB",
                null, mLocalStorage.getBundle(0));
    }

    @Test
    public void getBundle() {
        List<BundleGD> bundles = new ArrayList<>();

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            int j = i + 1;

            BundleGD bundle = new BundleGD();
            bundle.id = j;
            bundle.createTime = 100L;
            bundle.lastTimeGetPackage = 150L;

            bundles.add(bundle);
        }
        mLocalStorage.putBundle(bundles);

        compare2Elements(bundles.get(1), mLocalStorage.getBundle(2));
    }

    @Test
    public void getBundleWithWrongFormatBundleId() {
        List<BundleGD> bundles = new ArrayList<>();

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            int j = i + 1;

            BundleGD bundle = new BundleGD();
            bundle.id = j;
            bundle.createTime = 100L;
            bundle.lastTimeGetPackage = 150L;

            bundles.add(bundle);
        }
        mLocalStorage.putBundle(bundles);

        assertEquals("getBundle with wrong format bundleId",
                null, mLocalStorage.getBundle(-1));
    }

    @Test
    public void getBundleWithOversizedBundleId() {
        List<BundleGD> bundles = new ArrayList<>();

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            int j = i + 1;

            BundleGD bundle = new BundleGD();
            bundle.id = j;
            bundle.createTime = 100L;
            bundle.lastTimeGetPackage = 150L;

            bundles.add(bundle);
        }
        mLocalStorage.putBundle(bundles);

        assertEquals("getBundle with oversized bundleId",
                null, mLocalStorage.getBundle(21));
    }

    @Test
    public void putSentBundleSummaryWithNullParam() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        mLocalStorage.putSentBundleSummary(null);
        mLocalStorage.getSentBundleSummary().subscribe(new CustomObserver<>(result));

        assertEquals("putSentBundleSummary with null param", null, result.get(0));
    }

    @Test
    public void putSentBundleSummary() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        SentBundleSummaryDB sentBundle = new SentBundleSummaryDB();
        sentBundle.id = 1L;
        sentBundle.timeCreate = 1482716479L;
        sentBundle.totalOfSentAmount = 3L;
        sentBundle.totalOfSentBundle = 2L;

        mLocalStorage.putSentBundleSummary(sentBundle);
        mLocalStorage.getSentBundleSummary().subscribe(new CustomObserver<>(result));

        compare2Elements(sentBundle, result.get(0));
    }

    @Test
    public void putSentBundleSummaryWithAmountIsANegativeNumber() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        SentBundleSummaryDB sentBundle = new SentBundleSummaryDB();
        sentBundle.id = 1L;
        sentBundle.timeCreate = 1482716479L;
        sentBundle.totalOfSentAmount = -1L;
        sentBundle.totalOfSentBundle = 1L;

        mLocalStorage.putSentBundleSummary(sentBundle);
        mLocalStorage.getSentBundleSummary().subscribe(new CustomObserver<>(result));

        assertEquals("putSentBundleSummary with sent amount is a negative number", null, result.get(0));
    }

    @Test
    public void putSentBundleSummaryWithBundleIsANegativeNumber() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        SentBundleSummaryDB sentBundle = new SentBundleSummaryDB();
        sentBundle.id = 1L;
        sentBundle.timeCreate = 1482716479L;
        sentBundle.totalOfSentAmount = 1L;
        sentBundle.totalOfSentBundle = -1L;

        mLocalStorage.putSentBundleSummary(sentBundle);
        mLocalStorage.getSentBundleSummary().subscribe(new CustomObserver<>(result));

        assertEquals("putSentBundleSummary with sent bundle is a negative number", null, result.get(0));
    }

    @Test
    public void getSentBundleSummaryWithEmptyDB() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        mLocalStorage.getSentBundleSummary().subscribe(new CustomObserver<>(result));

        assertEquals("getSentBundleSummary with did't send bundle summary", null, result.get(0));
    }

    @Test
    public void getSentBundleSummary() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        for(int i = 0; i < 5; i++) {
            int j = i + 1;

            SentBundleSummaryDB sentBundle = new SentBundleSummaryDB();
            sentBundle.id = 1L + j;
            sentBundle.timeCreate = 1482716479L + j;
            sentBundle.totalOfSentAmount = 3L;
            sentBundle.totalOfSentBundle = 2L;
            mLocalStorage.putSentBundleSummary(sentBundle);
        }

        mLocalStorage.getSentBundleSummary().subscribe(new CustomObserver<>(result));

        SentBundleSummaryDB tmp = new SentBundleSummaryDB();
        tmp.id = 1L;
        tmp.timeCreate = 1482716479L;
        tmp.totalOfSentAmount = 3L;
        tmp.totalOfSentBundle = 2L;

        compare2Elements(tmp, result.get(0));
    }

    @Test
    public void putReceivePacketSummaryWithNullParam() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        mLocalStorage.putReceivePacketSummary(null);
        mLocalStorage.getReceivePacketSummary().subscribe(new CustomObserver<>(result));

        assertEquals("putReceivePacketSummary with null param", null, result.get(0));
    }

    @Test
    public void putReceivePacketSummary() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
        receivePacket.id = 1L;
        receivePacket.timeCreate = 1482716479L;
        receivePacket.totalOfLuckiestDraw = 3L;
        receivePacket.totalOfRevamount = 2L;
        receivePacket.totalOfRevPackage = 2L;

        mLocalStorage.putReceivePacketSummary(receivePacket);
        mLocalStorage.getReceivePacketSummary().subscribe(new CustomObserver<>(result));

        compare2Elements(receivePacket, result.get(0));
    }

    @Test
    public void putReceivePacketSummaryWithLuckiestDrawIsANegativeNumber() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
        receivePacket.id = 1L;
        receivePacket.timeCreate = 1482716479L;
        receivePacket.totalOfLuckiestDraw = -3L;
        receivePacket.totalOfRevamount = 2L;
        receivePacket.totalOfRevPackage = 2L;

        mLocalStorage.putReceivePacketSummary(receivePacket);
        mLocalStorage.getReceivePacketSummary().subscribe(new CustomObserver<>(result));

        assertEquals("putReceivePacketSummary with luckiest draw is a negative number", null, result.get(0));
    }

    @Test
    public void putReceivePacketSummaryWithRevmountIsANegativeNumber() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
        receivePacket.id = 1L;
        receivePacket.timeCreate = 1482716479L;
        receivePacket.totalOfLuckiestDraw = 3L;
        receivePacket.totalOfRevamount = -2L;
        receivePacket.totalOfRevPackage = 2L;

        mLocalStorage.putReceivePacketSummary(receivePacket);
        mLocalStorage.getReceivePacketSummary().subscribe(new CustomObserver<>(result));

        assertEquals("putReceivePacketSummary with revamount is a negative number", null, result.get(0));
    }

    @Test
    public void putReceivePacketSummaryWithRevPackageIsANegativeNumber() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
        receivePacket.id = 1L;
        receivePacket.timeCreate = 1482716479L;
        receivePacket.totalOfLuckiestDraw = -3L;
        receivePacket.totalOfRevamount = 2L;
        receivePacket.totalOfRevPackage = 2L;

        mLocalStorage.putReceivePacketSummary(receivePacket);
        mLocalStorage.getReceivePacketSummary().subscribe(new CustomObserver<>(result));

        assertEquals("putReceivePacketSummary with revpackage is a negative number", null, result.get(0));
    }

    @Test
    public void getReceivePacketSummaryWithNullParam() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        mLocalStorage.getReceivePacketSummary().subscribe(new CustomObserver<>(result));

        assertEquals("getReceivePacketSummary when didn't put receive packet summary", null, result.get(0));
    }

    @Test
    public void getReceivePacketSummary() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        for(int i = 0; i < 5; i++) {
            int j = i + 1;

            ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
            receivePacket.id = 1L;
            receivePacket.timeCreate = 1482716479L;
            receivePacket.totalOfLuckiestDraw = 3L;
            receivePacket.totalOfRevamount = 2L;
            receivePacket.totalOfRevPackage = 2L;
            mLocalStorage.putReceivePacketSummary(receivePacket);
        }

        mLocalStorage.getReceivePacketSummary().subscribe(new CustomObserver<>(result));

        ReceivePacketSummaryDB tmp = new ReceivePacketSummaryDB();
        tmp.id = 1L;
        tmp.timeCreate = 1482716479L;
        tmp.totalOfLuckiestDraw = 3L;
        tmp.totalOfRevamount = 2L;
        tmp.totalOfRevPackage = 2L;

        compare2Elements(tmp, result.get(0));
    }

    @Test
    public void putSentBundleWithNullParam() {
        final List<SentBundle> result = new ArrayList<SentBundle>();

        mLocalStorage.putSentBundle(null);
        mLocalStorage.getSentBundle(0, 5).subscribe(new DefaultObserver<>(result));

        assertEquals("putReceivePacketSummary with null param", 0, result.size());
    }

    @Test
    public void putSentBundle() {
        final List<SentBundle> result = new ArrayList<>();
        List<SentBundleGD> sendBundleGDs = new ArrayList<>();

        int limit = 5;
        int inputSize = 20;

        for(int i = 0; i < inputSize; i++) {
            SentBundleGD sentBundleGD = new SentBundleGD();
            sentBundleGD.id = 1L + i;
            sentBundleGD.senderZaloPayID = "sender";
            sentBundleGD.type = 5L;
            sentBundleGD.createTime = 1482716479L + Long.valueOf(i);
            sentBundleGD.lastOpenTime = 150L;
            sentBundleGD.totalLuck = 20000L;
            sentBundleGD.numOfOpenedPakages = 2L;
            sentBundleGD.numOfPackages = 5L;
            sentBundleGD.sendMessage = "message";
            sentBundleGD.status = 1L;
            sendBundleGDs.add(sentBundleGD);
        }

        mLocalStorage.putSentBundle(sendBundleGDs);
        mLocalStorage.getSentBundle(1482716485L, limit).subscribe(new DefaultObserver<>(result));

        for(int i = 0; i < limit; i++) {
            compare2Elements(sendBundleGDs.get(5 - i), result.get(i));
        }
    }

    @Test
    public void getSentBundleWithCreateTimeEqualsZero() {
        final List<SentBundle> result = new ArrayList<>();
        List<SentBundleGD> sendBundleGDs = new ArrayList<>();

        int limit = 5;
        int inputSize = 20;

        for(int i = 0; i < inputSize; i++) {
            int j = i + 1;

            SentBundleGD sentBundleGD = new SentBundleGD();
            sentBundleGD.id = 1L + j;
            sentBundleGD.senderZaloPayID = "sender";
            sentBundleGD.type = 5L;
            sentBundleGD.createTime = 1482716479L + i;
            sentBundleGD.lastOpenTime = 150L;
            sentBundleGD.totalLuck = 20000L;
            sentBundleGD.numOfOpenedPakages = 2L;
            sentBundleGD.numOfPackages = 5L;
            sentBundleGD.sendMessage = "message";
            sentBundleGD.status = 1L;
            sendBundleGDs.add(sentBundleGD);
        }

        mLocalStorage.putSentBundle(sendBundleGDs);
        mLocalStorage.getSentBundle(0, limit).subscribe(new DefaultObserver<>(result));

        for(int i = 0; i < limit; i++) {
            compare2Elements(sendBundleGDs.get(inputSize - 1 - i), result.get(i));
        }
    }

    @Test
    public void getSentBundleWithLimitEqualsZero() {
        final List<SentBundle> result = new ArrayList<SentBundle>();
        List<SentBundleGD> sendBundleGDs = new ArrayList<SentBundleGD>();

        int limit = 5;
        int inputSize = 25;

        for(int i = 0; i < inputSize; i++) {
            int j = i + 1;

            SentBundleGD sentBundleGD = new SentBundleGD();
            sentBundleGD.id = 1L + j;
            sentBundleGD.senderZaloPayID = "sender";
            sentBundleGD.type = 5L;
            sentBundleGD.createTime = 1482716479L + j;
            sentBundleGD.lastOpenTime = 150L;
            sentBundleGD.totalLuck = 20000L;
            sentBundleGD.numOfOpenedPakages = 2L;
            sentBundleGD.numOfPackages = 5L;
            sentBundleGD.sendMessage = "message";
            sentBundleGD.status = 1L;
            sendBundleGDs.add(sentBundleGD);
        }

        mLocalStorage.putSentBundle(sendBundleGDs);
        mLocalStorage.getSentBundle(1482716489L, 0).subscribe(new DefaultObserver<>(result));

        assertEquals("getSentBundle with limit = 0", 0, result.size());
    }

    @Test
    public void getSentBundleWithCreateTimeIsANegativeNumber() {
        final List<SentBundle> result = new ArrayList<SentBundle>();
        List<SentBundleGD> sendBundleGDs = new ArrayList<SentBundleGD>();

        int limit = 5;
        int inputSize = 25;

        for(int i = 0; i < inputSize; i++) {
            int j = i + 1;

            SentBundleGD sentBundleGD = new SentBundleGD();
            sentBundleGD.id = 1L + j;
            sentBundleGD.senderZaloPayID = "sender";
            sentBundleGD.type = 5L;
            sentBundleGD.createTime = 1482716479L + j;
            sentBundleGD.lastOpenTime = 150L;
            sentBundleGD.totalLuck = 20000L;
            sentBundleGD.numOfOpenedPakages = 2L;
            sentBundleGD.numOfPackages = 5L;
            sentBundleGD.sendMessage = "message";
            sentBundleGD.status = 1L;
            sendBundleGDs.add(sentBundleGD);
        }

        mLocalStorage.putSentBundle(sendBundleGDs);
        mLocalStorage.getSentBundle(-1482716489L, limit).subscribe(new DefaultObserver<>(result));

        assertEquals("getSentBundle with creatime < 0", 0, result.size());
    }

    @Test
    public void getSentBundleWithLimitIsANegativeNumber() {
        final List<SentBundle> result = new ArrayList<SentBundle>();
        List<SentBundleGD> sendBundleGDs = new ArrayList<SentBundleGD>();

        int inputSize = 25;

        for(int i = 0; i < inputSize; i++) {
            int j = i + 1;

            SentBundleGD sentBundleGD = new SentBundleGD();
            sentBundleGD.id = 1L + j;
            sentBundleGD.senderZaloPayID = "sender";
            sentBundleGD.type = 5L;
            sentBundleGD.createTime = 1482716479L + j;
            sentBundleGD.lastOpenTime = 150L;
            sentBundleGD.totalLuck = 20000L;
            sentBundleGD.numOfOpenedPakages = 2L;
            sentBundleGD.numOfPackages = 5L;
            sentBundleGD.sendMessage = "message";
            sentBundleGD.status = 1L;
            sendBundleGDs.add(sentBundleGD);
        }

        mLocalStorage.putSentBundle(sendBundleGDs);
        mLocalStorage.getSentBundle(0, -2).subscribe(new DefaultObserver<>(result));

        assertEquals("getSentBundle with limit < 0", 0, result.size());
    }

    @Test
    public void isHaveSentBundleInDbWithEmptyDB() {
        boolean result;

        result = mLocalStorage.isHaveSentBundleInDb(0, 1);
        assertEquals("isHaveSentBundleInDb with empty DB", false, result);
    }

    @Test
    public void isHaveSentBundleInDb() {
        boolean result;
        List<SentBundleGD> sentBundleGDs = new ArrayList<SentBundleGD>();

        int inputSize = 20;

        for(int i = 0; i < inputSize; i++) {
            int j = i + 1;

            SentBundleGD sentBundleGD = new SentBundleGD();
            sentBundleGD.id = 1L + j;
            sentBundleGD.senderZaloPayID = "sender";
            sentBundleGD.type = 5L;
            sentBundleGD.createTime = 1482716479L + j;
            sentBundleGD.lastOpenTime = 150L;
            sentBundleGD.totalLuck = 20000L;
            sentBundleGD.numOfOpenedPakages = 2L;
            sentBundleGD.numOfPackages = 5L;
            sentBundleGD.sendMessage = "message";
            sentBundleGD.status = 1L;
            sentBundleGDs.add(sentBundleGD);
        }

        mLocalStorage.putSentBundle(sentBundleGDs);

        result = mLocalStorage.isHaveSentBundleInDb(1482716489L, 1);
        assertEquals("isHaveSentBundleInDb when DB had datas", true, result);
    }

    @Test
    public void setBundleStatusWhenNotHavingAnyBundles() {
        List<SentBundle> result = new ArrayList<>();

        mLocalStorage.setBundleStatus(1, 1);
        mLocalStorage.getSentBundle(0, 1).subscribe(new DefaultObserver<>(result));
        assertEquals("setBundleStatus when not having any bundles", 0, result.size());
    }

    @Test
    public void setBundleStatusWithUndefinedBundleId() {
        List<SentBundle> result = new ArrayList<>();
        List<SentBundleGD> sendBundleGDs = new ArrayList<SentBundleGD>();

        int inputSize = 20;

        for(int i = 0; i < inputSize; i++) {
            int j = i + 1;

            SentBundleGD sentBundleGD = new SentBundleGD();
            sentBundleGD.id = 1L + j;
            sentBundleGD.senderZaloPayID = "sender";
            sentBundleGD.type = 5L;
            sentBundleGD.createTime = 1482716480L + j;
            sentBundleGD.lastOpenTime = 150L;
            sentBundleGD.totalLuck = 20000L;
            sentBundleGD.numOfOpenedPakages = 2L;
            sentBundleGD.numOfPackages = 5L;
            sentBundleGD.sendMessage = "message";
            sentBundleGD.status = 2L;
            sendBundleGDs.add(sentBundleGD);
        }

        mLocalStorage.putSentBundle(sendBundleGDs);
        mLocalStorage.setBundleStatus(22, 1);
        mLocalStorage.getSentBundle(0, 1).subscribe(new DefaultObserver<>(result));
        assertEquals("setBundleStatus with undefined bundle id", 2, result.get(0).status);
    }

    @Test
    public void setBundleStatus() {
        List<SentBundle> result = new ArrayList<>();
        List<SentBundleGD> sentBundleGDs = new ArrayList<SentBundleGD>();

        int inputSize = 20;

        for(int i = 0; i < inputSize; i++) {
            int j = i + 1;

            SentBundleGD sentBundleGD = new SentBundleGD();
            sentBundleGD.id = 1L + j;
            sentBundleGD.senderZaloPayID = "sender";
            sentBundleGD.type = 5L;
            sentBundleGD.createTime = 1482716480L + j;
            sentBundleGD.lastOpenTime = 150L;
            sentBundleGD.totalLuck = 20000L;
            sentBundleGD.numOfOpenedPakages = 2L;
            sentBundleGD.numOfPackages = 5L;
            sentBundleGD.sendMessage = "message";
            sentBundleGD.status = 2L;
            sentBundleGDs.add(sentBundleGD);
        }

        mLocalStorage.putSentBundle(sentBundleGDs);
        mLocalStorage.setBundleStatus(17, 1);
        mLocalStorage.getSentBundle(0, 5).subscribe(new DefaultObserver<>(result));
        assertEquals("setBundleStatus", 1, result.get(4).status);
    }

    @Test
    public void putReceivePackagesWithNullParam() {
        final List<ReceivePackage> result = new ArrayList<ReceivePackage>();

        mLocalStorage.putReceivePackages(null);
        mLocalStorage.getReceiveBundle(0, 5).subscribe(new DefaultObserver<>(result));

        assertEquals("putReceivePacketSummary with null param", 0, result.size());
    }

    @Test
    public void putReceivePackages() {
        final List<ReceivePackage> result = new ArrayList<ReceivePackage>();
        List<ReceivePackageGD> receivePackageGDs = new ArrayList<ReceivePackageGD>();

        int limit = 5;
        int inputSize = 20;

        for(int i = 0; i < inputSize; i++) {
            ReceivePackageGD receivePackageGD = new ReceivePackageGD();
            receivePackageGD.id = 1L + i;
            receivePackageGD.bundleID = 1L + i;
            receivePackageGD.receiverZaloPayID = "receiver";
            receivePackageGD.senderZaloPayID = "id";
            receivePackageGD.senderFullName = "name";
            receivePackageGD.senderAvatar = "ava";
            receivePackageGD.amount = 10000L;
            receivePackageGD.openedTime = 200L;
            receivePackageGD.status = 1L;
            receivePackageGD.messageStatus = "messStt";
            receivePackageGD.message = "mess";
            receivePackageGD.isLuckiest = 1L;
            receivePackageGD.createTime = 1482716480L + i;
            receivePackageGDs.add(receivePackageGD);
        }

        mLocalStorage.putReceivePackages(receivePackageGDs);
        mLocalStorage.getReceiveBundle(1482716490L, limit).subscribe(new DefaultObserver<>(result));

        for(int i = 0; i < limit; i++) {
            compare2Elements(receivePackageGDs.get(i), result.get(i));
        }
    }

    @Test
    public void getReceiveBundleWithCreateTimeEqualsZero() {
        final List<ReceivePackage> result = new ArrayList<ReceivePackage>();
        List<ReceivePackageGD> receivePackageGDs = new ArrayList<ReceivePackageGD>();

        int limit = 5;
        int inputSize = 25;

        for(int i = 0; i < inputSize; i++) {
            ReceivePackageGD receivePackageGD = new ReceivePackageGD();
            receivePackageGD.id = 1L + i;
            receivePackageGD.bundleID = 1L + i;
            receivePackageGD.receiverZaloPayID = "receiver";
            receivePackageGD.senderZaloPayID = "id";
            receivePackageGD.senderFullName = "name";
            receivePackageGD.senderAvatar = "ava";
            receivePackageGD.amount = 10000L;
            receivePackageGD.openedTime = 200L + i;
            receivePackageGD.status = 1L;
            receivePackageGD.messageStatus = "messStt";
            receivePackageGD.message = "mess";
            receivePackageGD.isLuckiest = 1L;
            receivePackageGD.createTime = 1482716480L + i;
            receivePackageGDs.add(receivePackageGD);
        }

        mLocalStorage.putReceivePackages(receivePackageGDs);
        mLocalStorage.getReceiveBundle(0, limit).subscribe(new DefaultObserver<>(result));

        for(int i = 0; i < limit; i++) {
            compare2Elements(receivePackageGDs.get(inputSize - 1 - i), result.get(i));
        }
    }

    @Test
    public void getReceiveBundleWithLimitEqualsZero() {
        final List<ReceivePackage> result = new ArrayList<ReceivePackage>();
        List<ReceivePackageGD> receivePackageGDs = new ArrayList<ReceivePackageGD>();

        int limit = 5;
        int inputSize = 25;

        for(int i = 0; i < inputSize; i++) {
            ReceivePackageGD receivePackageGD = new ReceivePackageGD();
            receivePackageGD.id = 1L + i;
            receivePackageGD.bundleID = 1L + i;
            receivePackageGD.receiverZaloPayID = "receiver";
            receivePackageGD.senderZaloPayID = "id";
            receivePackageGD.senderFullName = "name";
            receivePackageGD.senderAvatar = "ava";
            receivePackageGD.amount = 10000L;
            receivePackageGD.openedTime = 200L + i;
            receivePackageGD.status = 1L;
            receivePackageGD.messageStatus = "messStt";
            receivePackageGD.message = "mess";
            receivePackageGD.isLuckiest = 1L;
            receivePackageGD.createTime = 1482716480L + i;
            receivePackageGDs.add(receivePackageGD);
        }

        mLocalStorage.putReceivePackages(receivePackageGDs);
        mLocalStorage.getReceiveBundle(1482716490L, 0).subscribe(new DefaultObserver<>(result));

        assertEquals("getReceiveBundle with limit = 0", 0, result.size());
    }

    @Test
    public void getReceiveBundleWithCreateTimeIsANegativeNumber() {
        final List<ReceivePackage> result = new ArrayList<ReceivePackage>();
        List<ReceivePackageGD> receivePackageGDs = new ArrayList<ReceivePackageGD>();

        int inputSize = 25;

        for(int i = 0; i < inputSize; i++) {
            ReceivePackageGD receivePackageGD = new ReceivePackageGD();
            receivePackageGD.id = 1L + i;
            receivePackageGD.bundleID = 1L + i;
            receivePackageGD.receiverZaloPayID = "receiver";
            receivePackageGD.senderZaloPayID = "id";
            receivePackageGD.senderFullName = "name";
            receivePackageGD.senderAvatar = "ava";
            receivePackageGD.amount = 10000L;
            receivePackageGD.openedTime = 200L + i;
            receivePackageGD.status = 1L;
            receivePackageGD.messageStatus = "messStt";
            receivePackageGD.message = "mess";
            receivePackageGD.isLuckiest = 1L;
            receivePackageGD.createTime = 1482716480L + i;
            receivePackageGDs.add(receivePackageGD);
        }

        mLocalStorage.putReceivePackages(receivePackageGDs);
        mLocalStorage.getReceiveBundle(-1482716480L, 1).subscribe(new DefaultObserver<>(result));

        assertEquals("getReceiveBundle with creatime < 0", 0, result.size());
    }

    @Test
    public void getReceiveBundleWithLimitIsANegativeNumber() {
        final List<ReceivePackage> result = new ArrayList<ReceivePackage>();
        List<ReceivePackageGD> receivePackageGDs = new ArrayList<ReceivePackageGD>();

        int inputSize = 25;

        for(int i = 0; i < inputSize; i++) {
            ReceivePackageGD receivePackageGD = new ReceivePackageGD();
            receivePackageGD.id = 1L + i;
            receivePackageGD.bundleID = 1L + i;
            receivePackageGD.receiverZaloPayID = "receiver";
            receivePackageGD.senderZaloPayID = "id";
            receivePackageGD.senderFullName = "name";
            receivePackageGD.senderAvatar = "ava";
            receivePackageGD.amount = 10000L;
            receivePackageGD.openedTime = 200L + i;
            receivePackageGD.status = 1L;
            receivePackageGD.messageStatus = "messStt";
            receivePackageGD.message = "mess";
            receivePackageGD.isLuckiest = 1L;
            receivePackageGD.createTime = 1482716480L + i;
            receivePackageGDs.add(receivePackageGD);
        }

        mLocalStorage.putReceivePackages(receivePackageGDs);
        mLocalStorage.getReceiveBundle(0, -5).subscribe(new DefaultObserver<>(result));

        assertEquals("getReceiveBundle with limit < 0", 0, result.size());
    }

    @Test
    public void isHaveReceivePacketInDbWithEmptyDB() {
        boolean result;

        result = mLocalStorage.isHaveReceivePacketInDb(0, 1);
        assertEquals("isHaveReceivePacketInDb with empty DB", false, result);
    }

    @Test
    public void isHaveReceivePacketInDb() {
        boolean result;
        List<ReceivePackageGD> receivePackageGDs = new ArrayList<ReceivePackageGD>();

        int inputSize = 25;

        for(int i = 0; i < inputSize; i++) {
            ReceivePackageGD receivePackageGD = new ReceivePackageGD();
            receivePackageGD.id = 1L + i;
            receivePackageGD.bundleID = 1L + i;
            receivePackageGD.receiverZaloPayID = "receiver";
            receivePackageGD.senderZaloPayID = "id";
            receivePackageGD.senderFullName = "name";
            receivePackageGD.senderAvatar = "ava";
            receivePackageGD.amount = 10000L;
            receivePackageGD.openedTime = 200L + i;
            receivePackageGD.status = 1L;
            receivePackageGD.messageStatus = "messStt";
            receivePackageGD.message = "mess";
            receivePackageGD.isLuckiest = 1L;
            receivePackageGD.createTime = 1482716480L + i;
            receivePackageGDs.add(receivePackageGD);
        }

        mLocalStorage.putReceivePackages(receivePackageGDs);

        result = mLocalStorage.isHaveReceivePacketInDb(1482716490L, 1);
        assertEquals("isHaveReceivePacketInDb when DB had datas", true, result);
    }

    @Test
    public void putPackageInBundleWithNullParam() {
        final List<PackageInBundle> result = new ArrayList<PackageInBundle>();

        mLocalStorage.putPackageInBundle(null);
        mLocalStorage.getPackageInBundle(0).subscribe(new DefaultObserver<>(result));

        assertEquals("putPackageInBundle with null param", 0, result.size());
    }

    @Test
    public void putPackageInBundleWithEmptyList() {
        final List<PackageInBundle> result = new ArrayList<PackageInBundle>();

        List<PackageInBundleGD> packageInBundleGD = new ArrayList<PackageInBundleGD>();

        mLocalStorage.putPackageInBundle(packageInBundleGD);
        mLocalStorage.getPackageInBundle(0).subscribe(new DefaultObserver<>(result));

        assertEquals("putPackageInBundle with empty list", 0, result.size());
    }

    @Test
    public void putPackageInBundle() {
        final List<PackageInBundle> result = new ArrayList<PackageInBundle>();

        List<PackageInBundleGD> packageInBundleList = new ArrayList<PackageInBundleGD>();

        for(int i = 0; i < 25; i++) {
            PackageInBundleGD packageInBundleGD = new PackageInBundleGD();
            packageInBundleGD.id = 1L + i;
            packageInBundleGD.bundleID = 2L;
            packageInBundleGD.amount = 2L;
            packageInBundleGD.isLuckiest = 1L;
            packageInBundleGD.openTime = 1241235L + i;
            packageInBundleGD.revAvatarURL = "Ava";
            packageInBundleGD.revFullName = "Full name";
            packageInBundleGD.revZaloID = 1L;
            packageInBundleGD.revZaloPayID = "ZaloPayID";
            packageInBundleGD.sendMessage = "Message";

            packageInBundleList.add(packageInBundleGD);
        }

        mLocalStorage.putPackageInBundle(packageInBundleList);
        mLocalStorage.getPackageInBundle(2).subscribe(new DefaultObserver<>(result));

        for(int i = 0; i < result.size(); i++) {
            compare2Elements(packageInBundleList.get(i), result.get(i));
        }
    }

    @Test
    public void putRedPacketAppInfoWithNullParam() {
        mLocalStorage.putRedPacketAppInfo(null);
        RedPacketAppInfo result = mLocalStorage.getRedPacketAppInfo();
        assertEquals("putRedPacketAppInfo with null param", null, result);
    }

    @Test
    public void putRedPacketAppInfoWithNullAppConfig() {
        RedPacketAppInfo redPacketAppInfo = new RedPacketAppInfo();
        redPacketAppInfo.checksum = "checksum";
        redPacketAppInfo.expiredTime = 12334543L;
        redPacketAppInfo.isUpdateAppInfo = true;
        redPacketAppInfo.appConfigEntity = new AppConfigEntity();

        mLocalStorage.putRedPacketAppInfo(redPacketAppInfo);
        RedPacketAppInfo result = mLocalStorage.getRedPacketAppInfo();

        compare2Elements(redPacketAppInfo, result);
    }

    @Test
    public void getRedPacketAppInfoWhenNotPutAppInfo() {
        RedPacketAppInfo result = mLocalStorage.getRedPacketAppInfo();
        assertEquals("getRedPacketAppInfo when not putting app info", null, result);
    }

    @Test
    public void getRedPacketAppInfoWithNullAppConfig() {
        RedPacketAppInfo redPacketAppInfo = new RedPacketAppInfo();
        redPacketAppInfo.checksum = "checksum";
        redPacketAppInfo.expiredTime = 12334543L;
        redPacketAppInfo.isUpdateAppInfo = true;

        RedPacketAppInfo result = mLocalStorage.getRedPacketAppInfo();
        assertEquals("getRedPacketAppInfo with null app config", null, result);
    }

    @Test
    public void setPacketStatusWhenNotHavingPacket() {
        mLocalStorage.setPacketStatus(1, 3, 1, "message");
        ReceivePackageGD result = mLocalStorage.getPacketStatus(1);

        assertEquals("setPacketStatus when not having packet", null, result);
    }

    @Test
    public void setPacketStatus() {
        mLocalStorage.addReceivedRedPacket(1, 1, "name", "ava", "mess");
        mLocalStorage.setPacketStatus(1, 3, 1, "message");
        ReceivePackageGD result = mLocalStorage.getPacketStatus(1);

        assertEquals("id", 1, result.id);
        assertEquals("amount", (Long) 3L, result.amount);
        assertEquals("status", (Long) 1L, result.status);
        assertEquals("message", "message", result.messageStatus);
    }

    @Test
    public void getPacketStatusWhenNotHavingPacket() {
        ReceivePackageGD result = mLocalStorage.getPacketStatus(1);

        assertEquals("getPacketStatus when not having packet", null, result);
    }

    @Test
    public void getPacketStatusWhenNotSetting() {
        mLocalStorage.addReceivedRedPacket(1, 1, "name", "ava", "mess");
        ReceivePackageGD result = mLocalStorage.getPacketStatus(1);

        assertEquals("id", 1, result.id);
        assertEquals("amount", null, result.amount);
        assertEquals("status", (Long) 1L, result.status);
        assertEquals("message", null, result.messageStatus);
    }

    @Test
    public void addReceivedRedPacket() {
        mLocalStorage.addReceivedRedPacket(1, 1, "name", "ava", "mess");
        ReceivePackage result = mLocalStorage.getReceivedPacket(1);

        assertEquals("packageID", 1, result.packageID);
        assertEquals("bundleID", 1, result.bundleID);
        assertEquals("senderFullName", "name", result.senderFullName);
        assertEquals("senderAvatar", "ava", result.senderAvatar);
        assertEquals("message", "mess", result.message);
    }

    @Test
    public void addReceivedRedPacketWhenAlreadyHave() {
        mLocalStorage.addReceivedRedPacket(1, 1, "name", "ava", "mess");
        mLocalStorage.addReceivedRedPacket(1, 3, "name1", "ava1", "mess1");
        ReceivePackage result = mLocalStorage.getReceivedPacket(1);

        assertEquals("packageID", 1, result.packageID);
        assertEquals("bundleID", 3, result.bundleID);
        assertEquals("senderFullName", "name1", result.senderFullName);
        assertEquals("senderAvatar", "ava1", result.senderAvatar);
        assertEquals("message", "mess1", result.message);
    }

    @Test
    public void getReceivedPacketWhenNotHavingPacket() {
        ReceivePackage result = mLocalStorage.getReceivedPacket(1);

        assertEquals("getReceivedPacket when not having packet", null, result);
    }

    private void compare2Elements(BundleGD b1, BundleGD b2) {
        if (b1 == null && b2 != null) {
            fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            fail("Compare null and non-null object");
            return;
        }

        assertEquals("id", b1.id, b2.id);
        assertEquals("createTime", b1.createTime, b2.createTime);
        assertEquals("lastTimeGetPackage", b1.lastTimeGetPackage, b2.lastTimeGetPackage);
    }

    private void compare2Elements(SentBundleSummaryDB b1, GetSentBundle b2) {
        if (b1 == null && b2 != null) {
            fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            fail("Compare null and non-null object");
            return;
        }

        assertEquals("totalOfSentBundle", (long) b1.totalOfSentBundle, b2.totalofsentbundle);
        assertEquals("totalOfSentAmount", (long) b1.totalOfSentAmount, b2.totalofsentamount);
    }

    private void compare2Elements(ReceivePacketSummaryDB b1, GetReceivePacket b2) {
        if (b1 == null && b2 != null) {
            fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            fail("Compare null and non-null object");
            return;
        }

        assertEquals("totalOfLuckiestDraw", (long) b1.totalOfLuckiestDraw, b2.numofluckiestdraw);
        assertEquals("totalOfRevamount", (long) b1.totalOfRevamount, b2.totalofrevamount);
        assertEquals("totalOfRevPackage", (long) b1.totalOfRevPackage, b2.totalofrevpackage);
    }

    private void compare2Elements(SentBundleGD b1, SentBundle b2) {
        if (b1 == null && b2 != null) {
            fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            fail("Compare null and non-null object");
            return;
        }

        assertEquals("id", b1.id, b2.bundleID);
        assertEquals("senderZaloPayID", b1.senderZaloPayID, b2.sendZaloPayID);
        assertEquals("type", (long) b1.type, b2.type);
        assertEquals("createTime", (long) b1.createTime, b2.createTime);
        assertEquals("lastOpenTime", (long) b1.lastOpenTime, b2.lastOpenTime);
        assertEquals("totalLuck", (long) b1.totalLuck, b2.totalLuck);
        assertEquals("numOfOpenedPakages", (long) b1.numOfOpenedPakages, b2.numOfOpenedPakages);
        assertEquals("numOfPackages", (long) b1.numOfPackages, b2.numOfPackages);
        assertEquals("sendMessage", b1.sendMessage, b2.sendMessage);
        assertEquals("status", (long) b1.status, b2.status);
    }

    private void compare2Elements(ReceivePackageGD b1, ReceivePackage b2) {
        if (b1 == null && b2 != null) {
            fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            fail("Compare null and non-null object");
            return;
        }

        assertEquals("id", b1.id, b2.packageID);
        assertEquals("bundleID", (long) b1.bundleID, b2.bundleID);
        assertEquals("receiverZaloPayID", b1.receiverZaloPayID, b2.revZaloPayID);
        assertEquals("senderZaloPayID", b1.senderZaloPayID, b2.senderZaloPayID);
        assertEquals("senderFullName", b1.senderFullName, b2.senderFullName);
        assertEquals("senderAvatar", b1.senderAvatar, b2.senderAvatar);
        assertEquals("amount", (long) b1.amount, b2.amount);
        assertEquals("openedTime", (long) b1.openedTime, b2.openedTime);
        assertEquals("status", (long) b1.status, b2.status);
        assertEquals("message", b1.message, b2.message);
        assertEquals("isLuckiest", (long) b1.isLuckiest, b2.isLuckiest);
        assertEquals("createTime", (long) b1.createTime, b2.createTime);
    }

    private void compare2Elements(PackageInBundleGD b1, PackageInBundle b2) {
        if (b1 == null && b2 != null) {
            fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            fail("Compare null and non-null object");
            return;
        }

        assertEquals("amount", (long) b1.amount, b2.amount);
        assertEquals("sendMessage", b1.sendMessage, b2.sendMessage);
        assertEquals("revZaloPayID", b1.revZaloPayID, b2.revZaloPayID);
        assertEquals("revZaloID", (long) b1.revZaloID, b2.revZaloID);
        if(b1.isLuckiest == 1) {
            assertEquals("isLuckiest", true, b2.isLuckiest);
        }
        else if(b1.isLuckiest == 0) {
            assertEquals("isLuckiest", false, b2.isLuckiest);
        }
        assertEquals("revFullName", b1.revFullName, b2.revFullName);
        assertEquals("revAvatarURL", b1.revAvatarURL, b2.revAvatarURL);
        assertEquals("bundleID", (long) b1.bundleID, b2.bundleID);
        assertEquals("openTime", (long) b1.openTime, b2.openTime);
        assertEquals("id", (long) b1.id, b2.packageID);
    }

    private void compare2Elements(RedPacketAppInfo b1, RedPacketAppInfo b2) {
        if (b1 == null && b2 != null) {
            fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            fail("Compare null and non-null object");
            return;
        }

        assertEquals("isUpdateAppInfo", false, b2.isUpdateAppInfo);
        assertEquals("expiredTime", b1.expiredTime, b2.expiredTime);
        assertEquals("checksum", b1.checksum, b2.checksum);
        assertEquals("appConfigEntity.bundleExpiredTime", b1.appConfigEntity.bundleExpiredTime, b2.appConfigEntity.bundleExpiredTime);
        assertEquals("appConfigEntity.maxAmountPerPackage", b1.appConfigEntity.maxAmountPerPackage, b2.appConfigEntity.maxAmountPerPackage);
        assertEquals("appConfigEntity.maxCountHist", b1.appConfigEntity.maxCountHist, b2.appConfigEntity.maxCountHist);
        assertEquals("appConfigEntity.maxMessageLength", b1.appConfigEntity.maxMessageLength, b2.appConfigEntity.maxMessageLength);
        assertEquals("appConfigEntity.maxPackageQuantity", b1.appConfigEntity.maxPackageQuantity, b2.appConfigEntity.maxPackageQuantity);
        assertEquals("appConfigEntity.maxTotalAmountPerBundle", b1.appConfigEntity.maxTotalAmountPerBundle, b2.appConfigEntity.maxTotalAmountPerBundle);
        assertEquals("appConfigEntity.minAmountEach", b1.appConfigEntity.minAmountEach, b2.appConfigEntity.minAmountEach);
        assertEquals("appConfigEntity.minDivideAmount", b1.appConfigEntity.minDivideAmount, b2.appConfigEntity.minDivideAmount);
    }
}
