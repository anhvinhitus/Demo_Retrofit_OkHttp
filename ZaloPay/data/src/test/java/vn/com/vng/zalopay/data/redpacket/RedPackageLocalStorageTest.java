package vn.com.vng.zalopay.data.redpacket;

import android.database.sqlite.SQLiteDatabase;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.cache.model.BundleGD;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePacketSummaryDB;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDB;
import vn.com.vng.zalopay.domain.model.redpacket.AppConfigEntity;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;

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
    public void putEmptyList() {
        List<BundleGD> bundles = new ArrayList<>();

        assertEquals("put empty list", null, mLocalStorage.getBundle(1));
    }

    @Test
    public void put() {
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
    public void getTransactionWithEmptyDB() {
        assertEquals("getBundle with empty DB",
                null, mLocalStorage.getBundle(0));
    }

    @Test
    public void getTransaction() {
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
    public void getTransactionWithWrongFormatBundleId() {
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
    public void getTransactionWithOversizedBundleId() {
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
        mLocalStorage.getSentBundleSummary().subscribe(new Observer<GetSentBundle>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print("Got error: " + e + "\n");
            }

            @Override
            public void onNext(GetSentBundle getSentBundle) {
                result.add(getSentBundle);
            }
        });

        assertEquals("putSentBundleSummary with null param", null, result.get(0));
    }

    @Test
    public void putSentBundleSummary() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        SentBundleSummaryDB sentBundle = new SentBundleSummaryDB();
        sentBundle.id = 1L;
        sentBundle.timeCreate = 12334543L;
        sentBundle.totalOfSentAmount = 3L;
        sentBundle.totalOfSentBundle = 2L;

        mLocalStorage.putSentBundleSummary(sentBundle);
        mLocalStorage.getSentBundleSummary().subscribe(new Observer<GetSentBundle>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print("Got error: " + e + "\n");
            }

            @Override
            public void onNext(GetSentBundle getSentBundle) {
                result.add(getSentBundle);
            }
        });

        compare2Elements(sentBundle, result.get(0));
    }

    @Test
    public void putSentBundleSummaryWithAmountIsANegativeNumber() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        SentBundleSummaryDB sentBundle = new SentBundleSummaryDB();
        sentBundle.id = 1L;
        sentBundle.timeCreate = 0L;
        sentBundle.totalOfSentAmount = -1L;
        sentBundle.totalOfSentBundle = 1L;

        mLocalStorage.putSentBundleSummary(sentBundle);
        mLocalStorage.getSentBundleSummary().subscribe(new Observer<GetSentBundle>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print("Got error: " + e + "\n");
            }

            @Override
            public void onNext(GetSentBundle getSentBundle) {
                result.add(getSentBundle);
            }
        });

        assertEquals("putSentBundleSummary with sent amount is a negative number", null, result.get(0));
    }

    @Test
    public void putSentBundleSummaryWithBundleIsANegativeNumber() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        SentBundleSummaryDB sentBundle = new SentBundleSummaryDB();
        sentBundle.id = 1L;
        sentBundle.timeCreate = 0L;
        sentBundle.totalOfSentAmount = 1L;
        sentBundle.totalOfSentBundle = -1L;

        mLocalStorage.putSentBundleSummary(sentBundle);
        mLocalStorage.getSentBundleSummary().subscribe(new Observer<GetSentBundle>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print("Got error: " + e + "\n");
            }

            @Override
            public void onNext(GetSentBundle getSentBundle) {
                result.add(getSentBundle);
            }
        });

        assertEquals("putSentBundleSummary with sent bundle is a negative number", null, result.get(0));
    }

    @Test
    public void getSentBundleSummaryWithEmptyDB() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        mLocalStorage.getSentBundleSummary().subscribe(new Observer<GetSentBundle>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print("Got error: " + e + "\n");
            }

            @Override
            public void onNext(GetSentBundle getSentBundle) {
                result.add(getSentBundle);
            }
        });

        assertEquals("getSentBundleSummary with did't send bundle summary", null, result.get(0));
    }

    @Test
    public void getSentBundleSummary() {
        final List<GetSentBundle> result = new ArrayList<GetSentBundle>();

        SentBundleSummaryDB sentBundle = new SentBundleSummaryDB();
        sentBundle.id = 1L;
        sentBundle.timeCreate = 12334543L;
        sentBundle.totalOfSentAmount = 3L;
        sentBundle.totalOfSentBundle = 2L;

        for(int i = 0; i < 5; i++) {
            sentBundle.timeCreate += 1;
            mLocalStorage.putSentBundleSummary(sentBundle);
        }

        mLocalStorage.getSentBundleSummary().subscribe(new Observer<GetSentBundle>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print("Got error: " + e + "\n");
            }

            @Override
            public void onNext(GetSentBundle getSentBundle) {
                result.add(getSentBundle);
            }
        });

        compare2Elements(sentBundle, result.get(0));
    }

    @Test
    public void putReceivePacketSummaryWithNullParam() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        mLocalStorage.putReceivePacketSummary(null);
        mLocalStorage.getReceivePacketSummary().subscribe(new Observer<GetReceivePacket>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print("Got error: " + e + "\n");
            }

            @Override
            public void onNext(GetReceivePacket getReceivePacket) {
                result.add(getReceivePacket);
            }
        });

        assertEquals("putReceivePacketSummary with null param", null, result.get(0));
    }

    @Test
    public void putReceivePacketSummary() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
        receivePacket.id = 1L;
        receivePacket.timeCreate = 12334543L;
        receivePacket.totalOfLuckiestDraw = 3L;
        receivePacket.totalOfRevamount = 2L;
        receivePacket.totalOfRevPackage = 2L;

        mLocalStorage.putReceivePacketSummary(receivePacket);
        mLocalStorage.getReceivePacketSummary().subscribe(new Observer<GetReceivePacket>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(GetReceivePacket getReceivePacket) {
                result.add(getReceivePacket);
            }
        });

        compare2Elements(receivePacket, result.get(0));
    }

    @Test
    public void putReceivePacketSummaryWithLuckiestDrawIsANegativeNumber() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
        receivePacket.id = 1L;
        receivePacket.timeCreate = 12334543L;
        receivePacket.totalOfLuckiestDraw = -3L;
        receivePacket.totalOfRevamount = 2L;
        receivePacket.totalOfRevPackage = 2L;

        mLocalStorage.putReceivePacketSummary(receivePacket);
        mLocalStorage.getReceivePacketSummary().subscribe(new Observer<GetReceivePacket>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(GetReceivePacket getReceivePacket) {
                result.add(getReceivePacket);
            }
        });

        assertEquals("putReceivePacketSummary with luckiest draw is a negative number", null, result.get(0));
    }

    @Test
    public void putReceivePacketSummaryWithRevmountIsANegativeNumber() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
        receivePacket.id = 1L;
        receivePacket.timeCreate = 12334543L;
        receivePacket.totalOfLuckiestDraw = 3L;
        receivePacket.totalOfRevamount = -2L;
        receivePacket.totalOfRevPackage = 2L;

        mLocalStorage.putReceivePacketSummary(receivePacket);
        mLocalStorage.getReceivePacketSummary().subscribe(new Observer<GetReceivePacket>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(GetReceivePacket getReceivePacket) {
                result.add(getReceivePacket);
            }
        });

        assertEquals("putReceivePacketSummary with revamount is a negative number", null, result.get(0));
    }

    @Test
    public void putReceivePacketSummaryWithRevPackageIsANegativeNumber() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
        receivePacket.id = 1L;
        receivePacket.timeCreate = 12334543L;
        receivePacket.totalOfLuckiestDraw = -3L;
        receivePacket.totalOfRevamount = 2L;
        receivePacket.totalOfRevPackage = 2L;

        mLocalStorage.putReceivePacketSummary(receivePacket);
        mLocalStorage.getReceivePacketSummary().subscribe(new Observer<GetReceivePacket>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(GetReceivePacket getReceivePacket) {
                result.add(getReceivePacket);
            }
        });

        assertEquals("putReceivePacketSummary with revpackage is a negative number", null, result.get(0));
    }

    @Test
    public void getReceivePacketSummaryWithNullParam() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        mLocalStorage.getReceivePacketSummary().subscribe(new Observer<GetReceivePacket>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print("Got error: " + e + "\n");
            }

            @Override
            public void onNext(GetReceivePacket getReceivePacket) {
                result.add(getReceivePacket);
            }
        });

        assertEquals("getReceivePacketSummary when didn't put receive packet summary", null, result.get(0));
    }

    @Test
    public void getReceivePacketSummary() {
        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();

        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
        receivePacket.id = 1L;
        receivePacket.timeCreate = 12334543L;
        receivePacket.totalOfLuckiestDraw = 3L;
        receivePacket.totalOfRevamount = 2L;
        receivePacket.totalOfRevPackage = 2L;

        for(int i = 0; i < 5; i++) {
            receivePacket.timeCreate += 1;
            mLocalStorage.putReceivePacketSummary(receivePacket);
        }

        mLocalStorage.getReceivePacketSummary().subscribe(new Observer<GetReceivePacket>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(GetReceivePacket getReceivePacket) {
                result.add(getReceivePacket);
            }
        });

        compare2Elements(receivePacket, result.get(0));
    }

//    @Test
//    public void putSentBundleWithNullParam() {
//        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();
//
//        mLocalStorage.putSentBundle(null);
//        mLocalStorage.getSentBundle().subscribe(new Observer<GetReceivePacket>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//            }
//
//            @Override
//            public void onNext(GetReceivePacket getReceivePacket) {
//                result.add(getReceivePacket);
//            }
//        });
//
//        assertEquals("putReceivePacketSummary with null param", null, result.get(0));
//    }
//
//    @Test
//    public void putSentBundle() {
//        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();
//
//        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
//        receivePacket.id = 1L;
//        receivePacket.timeCreate = 12334543L;
//        receivePacket.totalOfLuckiestDraw = 3L;
//        receivePacket.totalOfRevamount = 2L;
//        receivePacket.totalOfRevPackage = 2L;
//
//        mLocalStorage.putReceivePacketSummary(receivePacket);
//        mLocalStorage.getReceivePacketSummary().subscribe(new Observer<GetReceivePacket>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onNext(GetReceivePacket getReceivePacket) {
//                result.add(getReceivePacket);
//            }
//        });
//
//        compare2Elements(receivePacket, result.get(0));
//    }
//
//    @Test
//    public void putSentBundleWithLuckiestDrawIsANegativeNumber() {
//        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();
//
//        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
//        receivePacket.id = 1L;
//        receivePacket.timeCreate = 12334543L;
//        receivePacket.totalOfLuckiestDraw = -3L;
//        receivePacket.totalOfRevamount = 2L;
//        receivePacket.totalOfRevPackage = 2L;
//
//        mLocalStorage.putReceivePacketSummary(receivePacket);
//        mLocalStorage.getReceivePacketSummary().subscribe(new Observer<GetReceivePacket>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onNext(GetReceivePacket getReceivePacket) {
//                result.add(getReceivePacket);
//            }
//        });
//
//        assertEquals("putReceivePacketSummary with luckiest draw is a negative number", null, result.get(0));
//    }
//
//    @Test
//    public void putSentBundleWithRevmountIsANegativeNumber() {
//        final List<GetReceivePacket> result = new ArrayList<GetReceivePacket>();
//
//        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
//        receivePacket.id = 1L;
//        receivePacket.timeCreate = 12334543L;
//        receivePacket.totalOfLuckiestDraw = 3L;
//        receivePacket.totalOfRevamount = -2L;
//        receivePacket.totalOfRevPackage = 2L;
//
//        mLocalStorage.putReceivePacketSummary(receivePacket);
//        mLocalStorage.getReceivePacketSummary().subscribe(new Observer<GetReceivePacket>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onNext(GetReceivePacket getReceivePacket) {
//                result.add(getReceivePacket);
//            }
//        });
//
//        assertEquals("putReceivePacketSummary with revamount is a negative number", null, result.get(0));
//    }

//    @Test
//    public void isHaveSentBundleInDbWhenDBHasntInitialized() {
//        boolean result;
//
//        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
//        SQLiteDatabase db = openHelper.getWritableDatabase();
//        DaoSession daoSession = new DaoMaster(db).newSession();
//        RedPacketDataMapper mapper = new RedPacketDataMapper();
//        mLocalStorage = new RedPacketLocalStorage(daoSession, mapper);
//        result = mLocalStorage.isHaveSentBundleInDb();
//        assertEquals("isHaveTransactionInDb when DB hasn't initialized", false, result);
//    }
//
//    @Test
//    public void isHaveSentBundleInDb() {
//        initData();
//        insertTransaction();
//
//        boolean result;
//
//        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "zalopaytest.db", null);
//        SQLiteDatabase db = openHelper.getWritableDatabase();
//        DaoSession daoSession = new DaoMaster(db).newSession();
//        daoSession.getTransactionLogDao().insertOrReplaceInTx(transform(entities));
//        mLocalStorage = new TransactionLocalStorage(daoSession);
//        result = mLocalStorage.isHaveTransactionInDb();
//        assertEquals("isHaveTransactionInDb when DB had datas", true, result);
//    }
//
//    @Test
//    public void isHaveSentBundleInDbWithEmptyDB() {
//        initData();
//        insertTransaction();
//
//        boolean result;
//
//        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "zalopaytest.db", null);
//        SQLiteDatabase db = openHelper.getWritableDatabase();
//        DaoSession daoSession = new DaoMaster(db).newSession();
//        daoSession.getTransactionLogDao().insertOrReplaceInTx(transform(entities));
//        daoSession.getTransactionLogDao().deleteAll();
//        mLocalStorage = new TransactionLocalStorage(daoSession);
//        result = mLocalStorage.isHaveTransactionInDb();
//        assertEquals("isHaveTransactionInDb when DB has deleted all datas", false, result);
//    }

    @Test
    public void putPackageInBundleWithNullParam() {
        final List<PackageInBundle> result = new ArrayList<PackageInBundle>();

        mLocalStorage.putPackageInBundle(null);
        mLocalStorage.getPackageInBundle(0).subscribe(new Observer<List<PackageInBundle>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print("Got error: " + e + "\n");
            }

            @Override
            public void onNext(List<PackageInBundle> packageInBundles) {
                result.addAll(packageInBundles);
            }
        });

        assertEquals("putPackageInBundle with null param", 0, result.size());
    }

    @Test
    public void putPackageInBundleWithEmptyList() {
        final List<PackageInBundle> result = new ArrayList<PackageInBundle>();

        List<PackageInBundleGD> packageInBundleGD = new ArrayList<PackageInBundleGD>();

        mLocalStorage.putPackageInBundle(packageInBundleGD);
        mLocalStorage.getPackageInBundle(0).subscribe(new Observer<List<PackageInBundle>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print("Got error: " + e + "\n");
            }

            @Override
            public void onNext(List<PackageInBundle> packageInBundles) {
                result.addAll(packageInBundles);
            }
        });

        assertEquals("putPackageInBundle with empty list", 0, result.size());
    }

    @Test
    public void putPackageInBundle() {
        final List<PackageInBundle> result = new ArrayList<PackageInBundle>();

        List<PackageInBundleGD> packageInBundleList = new ArrayList<PackageInBundleGD>();

        PackageInBundleGD packageInBundleGD = new PackageInBundleGD();
        packageInBundleGD.id = 1L;
        packageInBundleGD.bundleID = 1L;
        packageInBundleGD.amount = 2L;
        packageInBundleGD.isLuckiest = 1L;
        packageInBundleGD.openTime = 1241235L;
        packageInBundleGD.revAvatarURL = "Ava";
        packageInBundleGD.revFullName = "Full name";
        packageInBundleGD.revZaloID = 1L;
        packageInBundleGD.revZaloPayID = "ZaloPayID";
        packageInBundleGD.sendMessage = "Message";

        packageInBundleList.add(packageInBundleGD);

        packageInBundleGD.bundleID = 2L;
        packageInBundleList.add(packageInBundleGD);

        packageInBundleGD.bundleID = 3L;
        packageInBundleList.add(packageInBundleGD);

        packageInBundleGD.bundleID = 1L;

        mLocalStorage.putPackageInBundle(packageInBundleList);
        mLocalStorage.getPackageInBundle(1).subscribe(new Observer<List<PackageInBundle>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<PackageInBundle> packageInBundles) {
                result.addAll(packageInBundles);
            }
        });

        compare2Elements(packageInBundleGD, result.get(0));
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
