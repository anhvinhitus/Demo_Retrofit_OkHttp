package vn.com.vng.zalopay.data.cache;

import android.text.TextUtils;
import android.util.LruCache;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.model.Person;

/**
 * Created by AnhHieu on 8/17/16.
 * Cache storage
 */
public class AccountLocalStorage extends SqlBaseScopeImpl implements AccountStore.LocalStorage {

    private final LruCache<String, Person> mCachePersonName = new LruCache<>(10);
    private final LruCache<String, Person> mCachePersonId = new LruCache<>(10);
    private final Gson mGson;

    public AccountLocalStorage(DaoSession daoSession, Gson gson) {
        super(daoSession);
        this.mGson = gson;

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

        JsonObject json = new JsonObject();
        json.addProperty("email", TextUtils.isEmpty(email) ? "" : email);
        json.addProperty("identity", TextUtils.isEmpty(identity) ? "" : identity);
        json.addProperty("foregroundImg", TextUtils.isEmpty(foregroundImg) ? "" : foregroundImg);
        json.addProperty("backgroundImg", TextUtils.isEmpty(backgroundImg) ? "" : backgroundImg);
        json.addProperty("avatarImg", TextUtils.isEmpty(avatarImg) ? "" : avatarImg);

        insertDataManifest("profile3info", json.toString());
    }

    @Override
    public void clearProfileInfo3() {
        // Chỉ xóa Image. Khi update success. Yêu cầu của QC
        Map<String, String> profile = getProfileInfo3();
        saveProfileInfo3(profile.get("email"), profile.get("identity"), null, null, null);
    }

    @Override
    public Map<String, String> getProfileInfo3() {
        Map<String, String> map = new HashMap<>();
        String temp = getDataManifest("profile3info");
        if (!TextUtils.isEmpty(temp)) {

            Type type = new TypeToken<Map<String, String>>() {
            }.getType();

            try {
                map = mGson.fromJson(temp, type);
            } catch (Exception e) {
                Timber.d(e, "exception");
            }

        }
        return map;
    }

    @Override
    public Map getProfileLevel2() {
        Map<String, String> map = new HashMap<>();
        String temp = getDataManifest(Constants.ProfileLevel2.PROFILE_LEVEL2);
        if (!TextUtils.isEmpty(temp)) {

            Type type = new TypeToken<Map<String, String>>() {
            }.getType();

            try {
                map = mGson.fromJson(temp, type);
            } catch (Exception e) {
                Timber.d(e, "exception");
            }

        }
        return map;
    }

    @Override
    public void saveProfileInfo2(String phoneNumber, boolean receiveOtp) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(Constants.ProfileLevel2.PHONE_NUMBER, TextUtils.isEmpty(phoneNumber) ? "" : phoneNumber);
        jsonObject.addProperty(Constants.ProfileLevel2.RECEIVE_OTP, receiveOtp);

        insertDataManifest(Constants.ProfileLevel2.PROFILE_LEVEL2, jsonObject.toString());
    }
}
