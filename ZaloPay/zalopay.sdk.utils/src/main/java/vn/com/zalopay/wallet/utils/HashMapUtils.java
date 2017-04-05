package vn.com.zalopay.wallet.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by sinhtt on 18/10/2016.
 */

public class HashMapUtils {
    private static Gson gson;

    public static TreeMap<String, String> JsonArrayToHashMap(JsonArray pArrays) {
        TreeMap<String, String> result;
        if (!pArrays.isJsonNull()) {
            result = new TreeMap<String, String>();
            for (int i = 0; i < pArrays.size(); i++) {
                JsonObject jsonObject = pArrays.get(i).getAsJsonObject();
                result.put(jsonObject.get("text").getAsString(), jsonObject.get("value").getAsString());
            }
            return result;
        }

        return null;
    }

    public static ArrayList<String> getKeys(TreeMap<String, String> pHashMap) {
        ArrayList<String> result;
        if (!pHashMap.isEmpty()) {
            result = new ArrayList<String>(pHashMap.keySet());
            return result;
        }
        return null;
    }
}
