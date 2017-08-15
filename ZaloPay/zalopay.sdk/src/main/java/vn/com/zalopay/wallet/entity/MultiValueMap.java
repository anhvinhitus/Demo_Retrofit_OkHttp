package vn.com.zalopay.wallet.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by chucvv on 7/26/17.
 */

public class MultiValueMap<K, V> {
    private Map<K, V> map = new HashMap<>();

    public V put(K key, V value) throws Exception {
        if (map == null) {
            throw new NullPointerException("map need to be initialized before use");
        }
        if (value instanceof String) {
            return map.put(key, value);
        }
        V existedValue = map.get(key);
        Collection values = existedValue != null ? (Collection) existedValue : new ArrayList();
        values.add(value);
        return map.put(key, (V) values);
    }

    public V get(K key) {
        return map != null ? map.get(key) : null;
    }

    public void clear() {
        if (map != null) {
            map.clear();
        }
    }

    public int size() {
        return map != null ? map.size() : 0;
    }

    public Set<K> keySet() {
        return map != null ? map.keySet() : null;
    }
}
