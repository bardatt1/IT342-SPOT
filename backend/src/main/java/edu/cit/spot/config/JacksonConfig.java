package edu.cit.spot.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalTime;

/**
 * Configuration for customizing Jackson JSON serialization/deserialization
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Module localTimeModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        return module;
    }

    /**
     * Custom deserializer for LocalTime to handle both string format and object format
     */
    public static class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            
            // If node is a string, parse it as a standard LocalTime string format
            if (node.isTextual()) {
                return LocalTime.parse(node.asText());
            }
            
            // If node is an object with hour, minute, second, nano properties
            if (node.isObject()) {
                int hour = node.has("hour") ? node.get("hour").asInt() : 0;
                int minute = node.has("minute") ? node.get("minute").asInt() : 0;
                int second = node.has("second") ? node.get("second").asInt() : 0;
                int nano = node.has("nano") ? node.get("nano").asInt() : 0;
                
                return LocalTime.of(hour, minute, second, nano);
            }
            
            // If node is a number, parse it as seconds of day (useful for timestamp formats)
            if (node.isNumber()) {
                int secondsOfDay = node.asInt();
                return LocalTime.ofSecondOfDay(secondsOfDay);
            }
            
            throw new IllegalArgumentException("Unable to deserialize LocalTime from: " + node);
        }
    }
}
