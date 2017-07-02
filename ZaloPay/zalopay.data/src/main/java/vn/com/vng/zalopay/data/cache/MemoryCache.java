package vn.com.vng.zalopay.data.cache;

import rx.Observable;

/**
 * Created by huuhoa on 7/1/17.
 * Declaration of memory cache
 */

public interface MemoryCache {
    static Object EmptyObject = new Object();
    /**
     * get an object from cache.
     * @param key
     * @return EmptyObject if key is not found
     * @throws NullPointerException if the specified key is null
     */
    Object get(String key);

    /**
     * put an object to cache
     * @param key
     * @param value
     * @throws NullPointerException if the specified key is null or value is null
     */
    void put(String key, Object value);

    /**
     * get an object with RxJava interface
     * @param key
     * @return OnError if key is null
     */
    Observable<Object> getObservable(String key);
}
