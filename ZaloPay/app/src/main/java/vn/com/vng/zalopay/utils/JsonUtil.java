package vn.com.vng.zalopay.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by longlv on 12/17/15.
 */
public class JsonUtil {

    @Deprecated
    public static <T> String toString(List<T> list, String parentheses) {
        if (list == null) return null;
        else {
            StringBuilder sb = new StringBuilder();
            if (parentheses != null) {
                sb.append(parentheses.charAt(0));
            }
            if (list.size() > 0) {
                sb.append(String.valueOf(list.get(0)));
                for (int i = 1; i < list.size(); i++) {
                    sb.append(",").append(String.valueOf(list.get(i)));
                }
            }
            if (parentheses != null) {
                sb.append(parentheses.charAt(1));
            }
            return sb.toString();
        }
    }

    @Deprecated
    public static <T> String toJsonArray(List<T> list) {
        if (list == null) return null;
        else {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            Gson gson = new Gson();
            if (list.size() > 0) {
                for (int i =0; i< list.size(); i++) {
                    sb.append(gson.toJson(list.get(i)));
                    if (i < list.size() -2) {
                        sb.append(",");
                    }
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public static <T> String toJsonArrayString(List<T> list) {
        if (list == null || list.size() <=0) {
            return "[]";
        }
        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(list, new TypeToken<List<T>>() {}.getType());

        if (!element.isJsonArray()) {
            return null;
        }
        return element.getAsJsonArray().toString();
    }
}

