package vn.com.vng.zalopay.data.location;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

/**
 * Created by khattn on 3/22/17.
 * Class helps save and get location from cache
 */

public class LocationLocalStorage extends SqlBaseScopeImpl implements LocationStore.LocalStorage {
    private final Gson mGson;

    public LocationLocalStorage(DaoSession daoSession, Gson gson) {
        super(daoSession);
        this.mGson = gson;
    }

    @Override
    public void save(double latitude, double longitude, String address, long timeget) {
        JsonObject json = new JsonObject();
        json.addProperty("latitude", latitude);
        json.addProperty("longitude", longitude);
        json.addProperty("address", TextUtils.isEmpty(address) ? "" : address);
        json.addProperty("timeget", timeget);

        insertDataManifest("location", json.toString());
    }

    @Override
    public Map<String, String> get() {
        Map<String, String> map = new HashMap<>();
        String temp = getDataManifest("location");
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
}