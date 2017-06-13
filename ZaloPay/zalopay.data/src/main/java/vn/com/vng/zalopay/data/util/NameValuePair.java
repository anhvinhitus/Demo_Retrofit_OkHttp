package vn.com.vng.zalopay.data.util;

/**
 * Created by huuhoa on 6/7/17.
 * NameValuePair for holding string-string mapping
 */

public final class NameValuePair {
    public final String key;
    public final String value;

    public NameValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NameValuePair that = (NameValuePair) o;

        if (!key.equals(that.key)) {
            return false;
        }

        return value.equals(that.value);

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NameValuePair{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
