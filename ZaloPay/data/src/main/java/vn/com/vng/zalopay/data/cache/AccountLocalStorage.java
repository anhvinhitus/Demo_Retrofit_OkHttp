package vn.com.vng.zalopay.data.cache;

import android.text.TextUtils;
import android.util.LruCache;

import vn.com.vng.zalopay.domain.model.Person;

/**
 * Created by AnhHieu on 8/17/16.
 */
public class AccountLocalStorage implements AccountStore.LocalStorage {

    LruCache<String, Person> mCachePerson = new LruCache<>(10);

    @Override
    public Person get(String zpName) {
        if (TextUtils.isEmpty(zpName)) {
            return null;
        }
        return mCachePerson.get(zpName);
    }

    @Override
    public void put(String zpName, Person person) {
        mCachePerson.put(zpName, person);
    }
}
