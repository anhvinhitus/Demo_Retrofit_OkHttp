package vn.com.vng.zalopay.data.friend;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.robolectric.RuntimeEnvironment;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.zfriend.FriendLocalStorage;
import vn.com.vng.zalopay.data.zfriend.FriendRepository;

/**
 * Created by hieuvm on 1/1/17.
 */

public class FriendRepositoryTest extends ApplicationTestCase {

    private FriendRepository mFriendRepository;

    private FriendLocalStorage mFriendLocalStorage;

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();

        mFriendLocalStorage = new FriendLocalStorage(daoSession);
    }
}
