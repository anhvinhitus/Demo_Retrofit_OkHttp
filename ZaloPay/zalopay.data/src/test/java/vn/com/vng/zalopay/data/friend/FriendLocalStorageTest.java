package vn.com.vng.zalopay.data.friend;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.zfriend.FriendLocalStorage;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.data.zfriend.contactloader.Contact;

/**
 * Created by hieuvm on 1/1/17.
 */

public class FriendLocalStorageTest extends ApplicationTestCase {

    private FriendStore.LocalStorage mFriendLocalStorage;

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();

        mFriendLocalStorage = new FriendLocalStorage(daoSession);
    }

    private ZaloUserEntity createZalo(long zaloId) {
        ZaloUserEntity entity = new ZaloUserEntity(zaloId);
        entity.displayName = "displayName";
        entity.avatar = "avatar";
        entity.usingApp = true;
        entity.userName = "userName";
        return entity;
    }

    private List<ZaloUserEntity> listZaloProfile(int length) {
        List<ZaloUserEntity> ret = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            ret.add(createZalo(ZALO_ID_START + i));
        }
        return ret;
    }

    private List<ZaloUserEntity> listZaloProfile() {
        return listZaloProfile(10);
    }

    private ZaloPayUserEntity createZaloPay(String zalopayId, long zaloId, long phonenumber) {
        ZaloPayUserEntity entity = new ZaloPayUserEntity();
        entity.phonenumber = phonenumber;
        entity.status = 1;
        entity.zaloid = String.valueOf(zaloId);
        entity.userid = zalopayId;
        entity.zalopayname = "zalopayname";
        return entity;
    }

    private static final long ZALO_ID_START = 900;
    private static final long PHONE_START = 123456;


    private List<ZaloPayUserEntity> listZaloPayProfile(int length) {
        List<ZaloPayUserEntity> ret = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            ret.add(createZaloPay("zp" + i, ZALO_ID_START + i, PHONE_START + i));
        }
        return ret;
    }

    private List<ZaloPayUserEntity> listZaloPayProfile() {
        return listZaloPayProfile(8);
    }

    private Contact createContact(String name, long phone) {
        Contact contact = new Contact("id", name);
        contact.addNumber(String.valueOf(phone), "type");
        return contact;
    }

    private List<Contact> listContact() {
        List<Contact> ret = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            ret.add(createContact("contact" + i, PHONE_START + i));
        }
        return ret;
    }

    @Test
    public void insertContact() {
        List<Contact> entry = listContact();
        mFriendLocalStorage.putContacts(entry);
        Assert.assertTrue(true);
    }

    @Test
    public void getCursorAllData() {
        List<ZaloUserEntity> zaloUser = listZaloProfile();
        mFriendLocalStorage.putZaloUser(zaloUser);

        List<ZaloPayUserEntity> entry = listZaloPayProfile();
        mFriendLocalStorage.putZaloPayUser(entry);


        List<Contact> contacts = listContact();
        mFriendLocalStorage.putContacts(contacts);

        Cursor cursor = mFriendLocalStorage.getZaloUserCursor(true);

        System.out.println("cursor : " + cursor + " " + cursor.getCount());

     /*   if (cursor.moveToNext()) {
            while (!cursor.isAfterLast()) {

                // System.out.println("  " + cursor.getString());
                for (String column : cursor.getColumnNames()) {
                    System.out.println(" columnName : [" + column + "]    value: [" + cursor.getString(cursor.getColumnIndex(column)) + "]");
                }
                System.out.println("----------------------------------------");

                cursor.moveToNext();
            }
        }*/
        Assert.assertTrue(cursor.getCount() == zaloUser.size());


    }

    @Test
    public void getZaloUserWithoutZaloPayId() {

        int length = 10;
        int length_without_zalopayid = 2;
        //Insert zalo user.
        List<ZaloUserEntity> zaloUser = listZaloProfile(length);
        mFriendLocalStorage.putZaloUser(zaloUser);

        List<Long> result = mFriendLocalStorage.getZaloUserWithoutZaloPayId();

        Assert.assertTrue(result.size() == length)
        ;
        for (int i = 0; i < length; i++) {
            Assert.assertTrue(zaloUser.get(i).userId == result.get(i));
        }

        //Insert zalopay user
        List<ZaloPayUserEntity> entry = listZaloPayProfile(length - length_without_zalopayid);
        mFriendLocalStorage.putZaloPayUser(entry);

        result = mFriendLocalStorage.getZaloUserWithoutZaloPayId();

        Assert.assertTrue(result.size() == length_without_zalopayid);
        for (int i = 0; i < length_without_zalopayid; i++) {
            Assert.assertTrue(result.get(i) == zaloUser.get(length - length_without_zalopayid + i).userId);
        }
    }

    @Test
    public void getRedPacketUsersEntity() {
        List<ZaloUserEntity> zaloUser = listZaloProfile();
        mFriendLocalStorage.putZaloUser(zaloUser);

        List<RedPacketUserEntity> result = mFriendLocalStorage.getRedPacketUsersEntity(Arrays.asList(ZALO_ID_START, ZALO_ID_START + 1, ZALO_ID_START + 2));
        System.out.println("result size : " + result.size());
        Assert.assertTrue(result.size() == 0);

        List<ZaloPayUserEntity> entry = listZaloPayProfile();
        mFriendLocalStorage.putZaloPayUser(entry);

        List<Contact> contacts = listContact();
        mFriendLocalStorage.putContacts(contacts);

        List<RedPacketUserEntity> result1 = mFriendLocalStorage.getRedPacketUsersEntity(Arrays.asList(ZALO_ID_START, ZALO_ID_START + 1, ZALO_ID_START + 2));
        System.out.println("result1 size : " + result1.size());
        Assert.assertTrue(result1.size() == 3);
    }


    @Test
    public void testUpdateContact() {
        List<ZaloUserEntity> zaloUser = listZaloProfile();
        mFriendLocalStorage.putZaloUser(zaloUser);

        List<ZaloPayUserEntity> entry = listZaloPayProfile();
        mFriendLocalStorage.putZaloPayUser(entry);


        List<Contact> contacts = listContact();
        mFriendLocalStorage.putContacts(contacts);

        long zaloId = 50;
        String zaloPayId = "12345678";
        long phone = 98765432;
        String nameInContact = "Mạnh Hiếu";


        ZaloUserEntity zalo = createZalo(zaloId);
        zalo.displayName = "hieuvm1234";

        ZaloPayUserEntity zaloPay = createZaloPay(zaloPayId, zaloId, phone);

        Contact contact = createContact(nameInContact, phone);

        mFriendLocalStorage.putZaloUser(Collections.singletonList(zalo));
        mFriendLocalStorage.putZaloPayUser(Collections.singletonList(zaloPay));
        mFriendLocalStorage.putContacts(Collections.singletonList(contact));

        Cursor cursor = mFriendLocalStorage.searchZaloFriendList("hieu", true);

        System.out.println("cursor : " + cursor.getCount());
        Assert.assertTrue(cursor.getCount() == 1);
        if (cursor.moveToNext()) {
            Assert.assertTrue(cursor.getString(cursor.getColumnIndex("ALIAS_DISPLAY_NAME")).equals(nameInContact));
        } else {
            Assert.fail("move to next fail");
        }

    }

}
