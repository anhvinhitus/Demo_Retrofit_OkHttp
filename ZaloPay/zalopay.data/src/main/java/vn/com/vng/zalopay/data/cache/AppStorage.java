package vn.com.vng.zalopay.data.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huuhoa on 3/28/17.
 * Interface for global-app scope storage in DB
 */
public interface AppStorage {
    /**
     * Put/Update a value into storage
     */
    void put(String key, String value);

    /**
     * Put/Update list of key-value entries into storage
     */
    void putAll(Map<String, String> map);

    /**
     * Get a value from storage
     * @return return defaultValue if key is not found in the storage
     */
    String get(String key, String defaultValue);

    /**
     * Get all key-value with list of keys
     */
    Map<String, String> getAll(String... keys);

    /**
     * Remove key-value entry given a key
     */
    void remove(String key);

    /**
     * Remove list of entries
     */
    void removeAll(String ...keys);
}
