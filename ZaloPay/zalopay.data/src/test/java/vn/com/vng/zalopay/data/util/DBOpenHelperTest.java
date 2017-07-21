package vn.com.vng.zalopay.data.util;

import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.AndroidApplicationTest;
import vn.com.vng.zalopay.data.BuildConfig;
import vn.com.vng.zalopay.data.CustomRobolectricRunner;
import vn.com.vng.zalopay.data.cache.model.ContactGD;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;

/**
 * Created by hieuvm on 7/13/17.
 * *
 */
@RunWith(CustomRobolectricRunner.class)
@Config(constants = BuildConfig.class, sdk = 16, application = AndroidApplicationTest.class)
public class DBOpenHelperTest {

    private DBOpenHelper mDBOpenHelper;
    private DaoSession mDaoSession;
    private DaoMaster mDaoMaster;

    private static final int LENGTH_TRANSFER = 10;

    @Before
    public void setUp() throws Exception {
        mDBOpenHelper = new DBOpenHelper(RuntimeEnvironment.application, "zalopay.db");
        mDaoMaster = new DaoMaster(mDBOpenHelper.getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
    }

    private List<TransferRecent> insertTransferRecent() {
        List<TransferRecent> list = new ArrayList<>();

        for (int i = 0; i < LENGTH_TRANSFER; i++) {
            TransferRecent transfer = new TransferRecent();
            transfer.zaloPayId = "123456" + i;
            transfer.zaloPayName = "zalopayname" + i;
            transfer.displayName = "display name " + i;
            transfer.avatar = "avatar " + i;
            list.add(transfer);
        }

        mDaoSession.getTransferRecentDao().insertInTx(list);
        return mDaoSession.getTransferRecentDao().loadAll();
    }

    private ContactGD insertContact() {
        ContactGD contact = new ContactGD();
        contact.phoneNumber = 1234567L;
        contact.displayName = "contact name";
        mDaoSession.getContactGDDao().insertInTx(contact);
        return mDaoSession.getContactGDDao().load(contact.phoneNumber);
    }

    @Test
    public void upgradeVersion214ToVersion215_Test() {
        List<TransferRecent> transfer = insertTransferRecent();
        ContactGD contact = insertContact();

        mDBOpenHelper.onUpgrade(mDBOpenHelper.getWritableDatabase(), DBOpenHelper.SCHEMA_VERSION_RELEASE_2_14, DBOpenHelper.SCHEMA_VERSION_RELEASE_2_15);
        mDaoSession = mDaoMaster.newSession();

        List<TransferRecent> recentResult = mDaoSession.getTransferRecentDao().loadAll();

        boolean assertTrue = Lists.elementsEqual(transfer, recentResult, (entity, result) -> {
            if (entity == null || result == null) {
                return false;
            }

            if (TextUtils.isEmpty(entity.zaloPayId)
                    || !entity.zaloPayId.equals(result.zaloPayId)) {
                return false;
            }

            if (TextUtils.isEmpty(entity.zaloPayName)
                    || !entity.zaloPayName.equals(result.zaloPayName)) {
                return false;
            }

            if (TextUtils.isEmpty(entity.displayName)
                    || !entity.displayName.equals(result.displayName)) {
                return false;
            }

            if (TextUtils.isEmpty(entity.avatar)
                    || !entity.avatar.equals(result.avatar)) {
                return false;
            }

            return true;
        });

        Assert.assertTrue(assertTrue);

        /*---------------------*/

        ContactGD contactResult = mDaoSession.getContactGDDao().load(contact.phoneNumber);
        Assert.assertTrue(contactResult == null);

    }

    @Test
    public void onUpgradeVersion10ToVersion215_Test() {

        int oldVersion = 1;

        List<TransferRecent> transfer = insertTransferRecent();
        ContactGD contact = insertContact();


        mDBOpenHelper.onUpgrade(mDBOpenHelper.getWritableDatabase(), oldVersion, DBOpenHelper.SCHEMA_VERSION_RELEASE_2_15);
        mDaoSession = mDaoMaster.newSession();

        List<TransferRecent> recentResult = mDaoSession.getTransferRecentDao().loadAll();
        Assert.assertTrue(recentResult == null || recentResult.size() == 0);

        /*---------------------*/

        ContactGD contactResult = mDaoSession.getContactGDDao().load(contact.phoneNumber);
        Assert.assertTrue(contactResult == null);
    }

}