package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;
import android.text.TextUtils;

import java.util.List;

import de.greenrobot.dao.query.LazyList;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGDDao;
import vn.com.vng.zalopay.data.util.Strings;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.LocalStorage
 */
public class FriendLocalStorage extends SqlBaseScopeImpl implements FriendStore.LocalStorage {
    private final ZaloFriendGDDao mDao;

    public FriendLocalStorage(DaoSession daoSession) {
        super(daoSession);
        mDao = daoSession.getZaloFriendGDDao();
    }

    @Override
    public void put(List<ZaloFriendGD> val) {
        mDao.insertOrReplaceInTx(val);
    }

    @Override
    public void writeZaloFriend(ZaloFriendGD val) {
        mDao.insertOrReplaceInTx(val);
    }

    @Override
    public List<ZaloFriendGD> listZaloFriend() {
        return mDao.queryBuilder().orderAsc(ZaloFriendGDDao.Properties.Fulltextsearch).list();
    }

    @Override
    public LazyList<ZaloFriendGD> listZaloFriend(String textSearch) {
        Timber.d("listZaloFriend textSearch: %s", textSearch);
        if (!TextUtils.isEmpty(textSearch)) {
            return mDao.queryBuilder().orderAsc(ZaloFriendGDDao.Properties.Fulltextsearch).where(ZaloFriendGDDao.Properties.Fulltextsearch.like("%" + Strings.stripAccents(textSearch).toLowerCase() + "%")).listLazy();
        } else {
            return mDao.queryBuilder().orderAsc(ZaloFriendGDDao.Properties.Fulltextsearch).listLazy();
        }
    }

    @Override
    public boolean isHaveZaloFriendDb() {
        return mDao.queryBuilder().count() > 0;
    }

    @Override
    public Cursor zaloFriendList() {
        return mDao.queryBuilder()
                .orderDesc(ZaloFriendGDDao.Properties.UsingApp)
                .orderAsc(ZaloFriendGDDao.Properties.Fulltextsearch)
                .buildCursor()
                .forCurrentThread()
                .query();
    }

    @Override
    public Cursor searchZaloFriendList(String s) {
        return mDao.queryBuilder()
                .where(ZaloFriendGDDao.Properties.Fulltextsearch.like("%" + Strings.stripAccents(s).toLowerCase() + "%"))
                .orderDesc(ZaloFriendGDDao.Properties.UsingApp)
                .orderAsc(ZaloFriendGDDao.Properties.Fulltextsearch)
                .buildCursor()
                .forCurrentThread()
                .query();
    }
}
