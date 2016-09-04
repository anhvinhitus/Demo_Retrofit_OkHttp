package vn.com.vng.zalopay.data.util;

import android.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.builder.ArrayBuilder;
import co.nstant.in.cbor.builder.MapBuilder;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.Number;
import co.nstant.in.cbor.model.UnicodeString;

/**
 * Created by huuhoa on 9/2/16.
 * Helper for data conversion between JSON and CBOR
 */

public class CBORHelper {
    private static HashMap<String, Long> JsonFieldToTag = new HashMap<>();
    private static HashMap<Long, String> TagToJsonField = new HashMap<>();
    static {
        JsonFieldToTag.put("type", 1L);
        JsonFieldToTag.put("uid", 2L);
        JsonFieldToTag.put("checksum", 3L);
        JsonFieldToTag.put("amount", 4L);
        JsonFieldToTag.put("message", 5L);

        TagToJsonField.put(1L, "type");
        TagToJsonField.put(2L, "uid");
        TagToJsonField.put(3L, "checksum");
        TagToJsonField.put(4L, "amount");
        TagToJsonField.put(5L, "message");
    }

    public static byte[] jsonToCbor(String json) throws CborException, IOException {
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(json).getAsJsonObject();

        CborBuilder builder = new CborBuilder();
        MapBuilder mapBuilder = builder.addMap();
        transformObject(object, mapBuilder);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CborEncoder encoder = new CborEncoder(byteArrayOutputStream);
        encoder.encode(builder.build());
        byte[] output = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return output;
    }

    public static String cborToJson(byte[] cbor) throws CborException, IOException {
        InputStream inputStream = new ByteArrayInputStream(cbor);
        CborDecoder decoder = new CborDecoder(inputStream);
        List<DataItem> dataItems = decoder.decode();

        DataItem item = dataItems.get(0);
        JsonObject root = new JsonObject();
        if (item instanceof Map) {
            transformCbor((Map) item, root);
        }

        return root.toString();
    }

    private static void transformCbor(Map item, JsonObject object) {
        for (DataItem key : item.getKeys()) {
            MajorType keyType = key.getMajorType();
            if (keyType != MajorType.UNSIGNED_INTEGER &&
                keyType != MajorType.NEGATIVE_INTEGER) {
                continue;
            }

            Number keyItem = (Number)key;
            System.out.print(String.format("keyValue: %s [%s]\n", keyType, keyItem));
            String keyName = TagToJsonField.get(keyItem.getValue().longValue());
            DataItem value = item.get(key);
            MajorType valueType = value.getMajorType();
            if (valueType == MajorType.BYTE_STRING) {
                String valueString = new String(((ByteString)value).getBytes());
                object.addProperty(keyName, valueString);
            } else if (valueType == MajorType.UNICODE_STRING) {
                String valueString = ((UnicodeString)value).getString();
                object.addProperty(keyName, valueString);
            } else if (valueType == MajorType.NEGATIVE_INTEGER ||
                    valueType == MajorType.UNSIGNED_INTEGER) {
                long valueNumber = ((Number)value).getValue().longValue();
                object.addProperty(keyName, valueNumber);
            }
        }
    }

    public static String toBase64(byte[] data) {
        return Base64.encodeToString(data,
                Base64.NO_PADDING | Base64.NO_WRAP | Base64.NO_CLOSE);
    }

    private static void transformObject(JsonObject jsonObject, MapBuilder mapBuilder) {
        for (java.util.Map.Entry<String, JsonElement> entry: jsonObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (!JsonFieldToTag.containsKey(entry.getKey())) {
                throw new TypeNotPresentException(entry.getKey(), null);
            }
            long key = JsonFieldToTag.get(entry.getKey());
            if (value.isJsonPrimitive()) {
                // convert to major type
                addPrimitiveToMap(mapBuilder, key, value);
            } else if (value.isJsonObject()) {
                MapBuilder newMap = mapBuilder.putMap(key);
                transformObject(value.getAsJsonObject(), newMap);
            } else if (value.isJsonArray()) {
                ArrayBuilder arrayBuilder = mapBuilder.putArray(key);
                transformArray(value, arrayBuilder);
            }
        }
    }

    private static void transformArray(JsonElement value, ArrayBuilder arrayBuilder) {
        JsonArray array = value.getAsJsonArray();
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                addPrimitiveToArray(arrayBuilder, element);
            } else if (element.isJsonObject()) {
                MapBuilder internalMap = arrayBuilder.addMap();
                transformObject(element.getAsJsonObject(), internalMap);
            } else if (element.isJsonArray()) {
                ArrayBuilder internalArray = arrayBuilder.addArray();
                transformArray(element, internalArray);
            }
        }
    }

    private static void addPrimitiveToMap(MapBuilder mapBuilder, long key, JsonElement value) {
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            mapBuilder.put(key, value.getAsBoolean());
        } else if (primitive.isNumber()) {
            mapBuilder.put(key, value.getAsLong());
        } else if (primitive.isString()) {
            mapBuilder.put(key, value.getAsString().getBytes());
        }
    }

    private static void addPrimitiveToArray(ArrayBuilder arrayBuilder, JsonElement value) {
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            arrayBuilder.add(value.getAsBoolean());
        } else if (primitive.isNumber()) {
            arrayBuilder.add(value.getAsLong());
        } else if (primitive.isString()) {
            arrayBuilder.add(value.getAsString().getBytes());
        }
    }
}
