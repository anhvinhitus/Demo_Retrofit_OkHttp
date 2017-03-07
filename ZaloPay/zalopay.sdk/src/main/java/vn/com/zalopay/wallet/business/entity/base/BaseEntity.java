package vn.com.zalopay.wallet.business.entity.base;

import com.google.gson.Gson;

/**
 * Super class for all entity in this SDK
 */
public abstract class BaseEntity<T> {

    /**
     * Parse the json string input to an {@link T} Object
     * representation of the same
     *
     * @param pJson The JSON string input
     * @return {@link T} Object
     */
    @SuppressWarnings("unchecked")
    public T fromJsonString(String pJson) {
        return (T) (new Gson()).fromJson(pJson, this.getClass());
    }

    /**
     * Auto-generate the JSON string for this instance
     *
     * @return JSON String
     */
    public String toJsonString() {
        return (new Gson()).toJson(this);
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
