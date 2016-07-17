package vn.com.vng.zalopay.mdl;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;

/**
 * Created by huuhoa on 7/14/16.
 * Internal helpers
 */
class Helpers {
    static String readableMapToString(ReadableMap param) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        ReadableMapKeySetIterator iterator = param.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            builder.append(key);
            builder.append(": ");
            ReadableType type = param.getType(key);
            switch (type) {
                case Boolean:
                    builder.append(param.getBoolean(key));
                    break;
                case String:
                    builder.append(param.getString(key));
                    break;
                case Number:
                    builder.append(param.getDouble(key));
                    break;
                case Null:
                    builder.append("NULL");
                case Map:
                    builder.append(readableMapToString(param.getMap(key)));
                    break;
                case Array: {
                    ReadableArray array = param.getArray(key);
                    builder.append(readableArrayToString(array));
                }
                default:
                    builder.append("UNSUPPORTED");
                    break;
            }
            builder.append(", ");
        }
        builder.append("}");
        return builder.toString();
    }

    static String readableArrayToString(ReadableArray param) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int index = 0; index < param.size(); index ++) {
            ReadableType type = param.getType(index);
            switch (type) {
                case Boolean:
                    builder.append(param.getBoolean(index));
                    break;
                case String:
                    builder.append(param.getString(index));
                    break;
                case Number:
                    builder.append(param.getDouble(index));
                    break;
                case Null:
                    builder.append("NULL");
                case Map:
                    builder.append(readableMapToString(param.getMap(index)));
                    break;
                case Array: {
                    ReadableArray array = param.getArray(index);
                    builder.append(readableArrayToString(array));
                }
                default:
                    builder.append("UNSUPPORTED");
                    break;
            }
            builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }
}
