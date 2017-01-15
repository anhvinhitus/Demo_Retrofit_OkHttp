package vn.com.vng.zalopay.data.ws.parser;

import android.util.Base64;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;

import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.model.NotificationEmbedData;

/**
 * Created by huuhoa on 8/29/16.
 * Notification message deserializer
 */
public class NotificationMessageDeserializer implements JsonDeserializer<NotificationEmbedData> {

    @Override
    public NotificationEmbedData deserialize(JsonElement value, Type type,
                                        JsonDeserializationContext context) throws JsonParseException {
        if (value.isJsonPrimitive()) {
            // try to decode base64 and convert to jsonobject
            String embedValue = value.getAsString();
            try {
                embedValue = new String(Base64.decode(embedValue, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE));
                JsonParser parser = new JsonParser();
                return new NotificationEmbedData(parser.parse(embedValue).getAsJsonObject());
            } catch (JsonParseException e) {
                Timber.d("Embed data is not JSON string [[%s]]", embedValue);
            } catch (Exception e) {
                Timber.d(e, "Embed data is bad");
            }
        } else {
            return new NotificationEmbedData(value.getAsJsonObject());
        }

        return new NotificationEmbedData(null);
    }

}
