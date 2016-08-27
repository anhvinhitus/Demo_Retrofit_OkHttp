package vn.com.vng.zalopay.data.cache;

import android.text.TextUtils;
import android.util.LruCache;

import vn.com.vng.zalopay.domain.model.Person;

/**
 * Created by AnhHieu on 8/17/16.
 * Cache storage
 */
public class AccountLocalStorage implements AccountStore.LocalStorage {

    private final LruCache<String, Person> mCachePersonName = new LruCache<>(10);
    private final LruCache<String, Person> mCachePersonId = new LruCache<>(10);

    @Override
    public Person get(String zpName) {
        if (TextUtils.isEmpty(zpName)) {
            return null;
        }
        return mCachePersonName.get(zpName);
    }

    @Override
    public Person getById(String zaloPayId) {
        if (TextUtils.isEmpty(zaloPayId)) {
            return null;
        }
        return mCachePersonId.get(zaloPayId);
    }

    @Override
    public void put(Person person) {
        if (!TextUtils.isEmpty(person.zalopayname)) {
            mCachePersonName.put(person.zalopayname, person);
        }

        if (!TextUtils.isEmpty(person.zaloPayId)) {
            mCachePersonId.put(person.zaloPayId, person);
        }
    }
}
