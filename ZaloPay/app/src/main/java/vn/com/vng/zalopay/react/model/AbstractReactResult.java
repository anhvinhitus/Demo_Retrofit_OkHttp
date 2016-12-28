package vn.com.vng.zalopay.react.model;

/**
 * Created by hieuvm on 12/27/16.
 */

public abstract class AbstractReactResult<T> {
    public int code;
    public String message;
    public T data;

    public AbstractReactResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
