package vn.com.vng.zalopay.data.redpacket;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;

import static junit.framework.Assert.assertEquals;

public class RedPackageLocalStorageTest extends ApplicationTestCase {

    private RedPacketStore.LocalStorage mLocalStorage;

    @Before
    public void setup() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new RedPacketLocalStorage(daoSession);
    }

    @Test
    public void setPacketStatusWhenNotHavingPacket() {
        mLocalStorage.setPacketStatus(1, 3, 1, "message");
        ReceivePackageGD result = mLocalStorage.getPacketStatus(1);

        assertEquals("setPacketStatus when not having packet", null, result);
    }

    @Test
    public void setPacketStatus() {
        mLocalStorage.setPacketStatus(1, 3, 1, "message");
        ReceivePackageGD result = mLocalStorage.getPacketStatus(1);

        assertEquals("id", 1, result.id);
        assertEquals("amount", (Long) 3L, result.amount);
        assertEquals("status", (Long) 1L, result.status);
        assertEquals("message", "message", result.messageStatus);
    }

    @Test
    public void updatePacketStatus() {
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
        ReceivePackageGD result = mLocalStorage.getPacketStatus(1);

        assertEquals("packageID", 1, result.id);
        assertEquals("bundleID", 1, (long) result.bundleID);
        assertEquals("senderFullName", "name", result.senderFullName);
        assertEquals("senderAvatar", "ava", result.senderAvatar);
        assertEquals("message", "mess", result.message);
    }

    @Test
    public void addReceivedRedPacketWhenAlreadyHave() {
        mLocalStorage.addReceivedRedPacket(1, 1, "name", "ava", "mess");
        mLocalStorage.addReceivedRedPacket(1, 3, "name1", "ava1", "mess1");
        ReceivePackageGD result = mLocalStorage.getPacketStatus(1);

        assertEquals("packageID", 1, result.id);
        assertEquals("bundleID", 3, (long) result.bundleID);
        assertEquals("senderFullName", "name1", result.senderFullName);
        assertEquals("senderAvatar", "ava1", result.senderAvatar);
        assertEquals("message", "mess1", result.message);
    }

    @Test
    public void getReceivedPacketWhenNotHavingPacket() {
        ReceivePackageGD result = mLocalStorage.getPacketStatus(1);

        assertEquals("getReceivedPacket when not having packet", null, result);
    }

}
