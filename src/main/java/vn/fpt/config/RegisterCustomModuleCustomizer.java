package vn.fpt.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import io.quarkus.vertx.runtime.jackson.JsonObjectSerializer;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Singleton;

@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {
    @Override
    public void customize(ObjectMapper objectMapper) {
        JsonFactory jsonFactory = JsonFactory.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build();

        // Register custom module for JsonObject
        SimpleModule module = new SimpleModule();
        module.addSerializer(JsonObject.class, new JsonObjectSerializer());
        module.addDeserializer(JsonObject.class, new JsonObjectDeserializer());

        objectMapper.copyWith(jsonFactory);
        objectMapper.registerModule(module);
        objectMapper.isEnabled(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION);
    }
}
