package vn.com.vng.zalopay.data.cache;

import android.text.TextUtils;
import android.util.LruCache;

import java.util.HashMap;
import java.util.Map;

import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.domain.model.Person;

/**
 * Created by AnhHieu on 8/17/16.
 * Cache storage
 */
public class AccountLocalStorage extends SqlBaseScopeImpl implements AccountStore.LocalStorage {

    private final LruCache<String, Person> mCachePersonName = new LruCache<>(10);
    private final LruCache<String, Person> mCachePersonId = new LruCache<>(10);

    public AccountLocalStorage(DaoSession daoSession) {
        super(daoSession);
    }

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

    @Override
    public void saveProfileInfo3(String email, String identity, String foregroundImg, String backgroundImg, String avatarImg) {
        insertDataManifest("email", email);
        insertDataManifest("identity", identity);

        if (foregroundImg != null) {
            insertDataManifest("foregroundImg", foregroundImg);
        }

        if (backgroundImg != null) {
            insertDataManifest("backgroundImg", backgroundImg);
        }

        if (avatarImg != null) {
            insertDataManifest("avatarImg", avatarImg);
        }
    }

    @Override
    public void clearProfileInfo3() {
        deleteByKey("email");
        deleteByKey("identity");
        deleteByKey("foregroundImg");
        deleteByKey("backgroundImg");
        deleteByKey("avatarImg");
    }

    @Override
    public Map getProfileInfo3() {
        Map<String, String> map = new HashMap<>();

        String email = getDataManifest("email");
        if (!TextUtils.isEmpty(email)) {
            map.put("email", email);
        }

        String identity = getDataManifest("identity");
        if (!TextUtils.isEmpty(identity)) {
            map.put("identity", identity);
        }

        String foregroundImg = getDataManifest("foregroundImg");
        if (!TextUtils.isEmpty(foregroundImg)) {
            map.put("foregroundImg", foregroundImg);
        }

        String backgroundImg = getDataManifest("backgroundImg");
        if (!TextUtils.isEmpty(backgroundImg)) {
            map.put("backgroundImg", backgroundImg);
        }

        String avatarImg = getDataManifest("avatarImg");
        if (!TextUtils.isEmpty(avatarImg)) {
            map.put("avatarImg", avatarImg);
        }

        return map;
    }
}
