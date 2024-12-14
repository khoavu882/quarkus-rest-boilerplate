package vn.fpt.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 7/8/24
 * Time: 8:24 AM
 */
public class JsonObjectDeserializer extends StdDeserializer<JsonObject> {

    public JsonObjectDeserializer() {
        this(null);
    }

    public JsonObjectDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public JsonObject deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        return new JsonObject(jp.readValueAsTree().toString());
    }
}
