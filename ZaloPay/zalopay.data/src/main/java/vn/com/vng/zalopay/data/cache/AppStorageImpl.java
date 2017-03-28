package vn.com.vng.zalopay.data.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.global.DaoSession;
import vn.com.vng.zalopay.data.cache.global.KeyValueGD;
import vn.com.vng.zalopay.data.cache.global.KeyValueGDDao;
import vn.com.vng.zalopay.data.util.Strings;

/**
 * Created by huuhoa on 3/28/17.
 * Interface for global-app scope storage in DB
 */
public class AppStorageImpl implements AppStorage {
    private final DaoSession daoSession;

    public AppStorageImpl(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    @Override
    public void put(String key, String value) {
        KeyValueGD item = new KeyValueGD();
        item.key = key;
        item.value = value;
        daoSession.getKeyValueGDDao().insertOrReplace(item);
    }

    @Override
    public void putAll(Map<String, String> map) {
        ArrayList<KeyValueGD> items = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            KeyValueGD item = new KeyValueGD();
            item.key = entry.getKey();
            item.value = entry.getValue();
        }

        daoSession.getKeyValueGDDao().insertOrReplaceInTx(items);
    }

    @Override
    public String get(String key, String defaultValue) {
        KeyValueGD item = daoSession.getKeyValueGDDao().queryBuilder()
                .where(KeyValueGDDao.Properties.Key.eq(key)).unique();
        if (item != null) {
            return item.value;
        }

        return defaultValue;
    }

    @Override
    public Map<String, String> getAll(String... keys) {
        try {
            List<KeyValueGD> result = daoSession.getKeyValueGDDao().queryRaw("WHERE " + KeyValueGDDao.Properties.Key.columnName + " IN (?)", Strings.joinWithDelimiter(",", keys));
            HashMap<String, String> retVal = new HashMap<>();
            for (KeyValueGD item : result) {
                retVal.put(item.key, item.value);
            }
            return retVal;
        } catch (Exception e) {
            Timber.w(e, "Error in querying values: %s", Arrays.asList(keys));
            return null;
        }
    }

    @Override
    public void remove(String key) {
        daoSession.getKeyValueGDDao().deleteByKey(key);
    }

    @Override
    public void removeAll(String... keys) {
        daoSession.getKeyValueGDDao().deleteByKeyInTx(keys);
    }
}
