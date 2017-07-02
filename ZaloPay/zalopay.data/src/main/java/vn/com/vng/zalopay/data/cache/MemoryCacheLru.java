package vn.com.vng.zalopay.data.cache;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import vn.com.vng.zalopay.data.util.ObservableHelper;

/**
 * Created by huuhoa on 7/1/17.
 * Implementation for memory cache
 */

public class MemoryCacheLru implements MemoryCache {
    private final Map<String, Object> mObjectMap = new HashMap<>();

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new NullPointerException("key should not be null");
        }

        if (mObjectMap.containsKey(key)) {
            return mObjectMap.get(key);
        }

        return EmptyObject;
    }

    @Override
    public void put(String key, Object value) {
        if (key == null || value == null) {
            throw new NullPointerException("key and value should not be null");
        }

        mObjectMap.put(key, value);
    }

    /**
     * get an object with RxJava interface
     *
     * @param key
     * @return OnError if key is null
     */
    @Override
    public Observable<Object> getObservable(String key) {
        return ObservableHelper.makeObservable(() -> get(key));
    }
}
