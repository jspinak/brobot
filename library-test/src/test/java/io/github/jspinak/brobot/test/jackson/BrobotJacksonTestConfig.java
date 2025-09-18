package io.github.jspinak.brobot.test.jackson;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * Provides properly configured ObjectMapper for Brobot tests. Handles serialization issues with: -
 * OpenCV/JavaCV Mat objects - SikuliX objects (Pattern, Region, Match) - BufferedImage -
 * LocalDateTime - Missing constructors/builders
 */
public class BrobotJacksonTestConfig {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Creates an ObjectMapper configured for Brobot model serialization/deserialization. This
     * mapper handles all the special cases and problematic types.
     */
    public static ObjectMapper createTestObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Basic configuration
        configureBasicFeatures(mapper);

        // Register modules
        registerJavaTimeModule(mapper);
        registerBrobotModule(mapper);

        return mapper;
    }

    private static void configureBasicFeatures(ObjectMapper mapper) {
        // Don't fail on unknown properties (forward compatibility)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Don't fail on empty beans
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Allow deserialization of single values as arrays
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        // Write dates as timestamps
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Include non-null values only
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Use field visibility
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);

        // Enable default typing for polymorphic types
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private static void registerJavaTimeModule(ObjectMapper mapper) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Custom LocalDateTime serializer/deserializer
        javaTimeModule.addSerializer(
                LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(
                LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));

        mapper.registerModule(javaTimeModule);
    }

    private static void registerBrobotModule(ObjectMapper mapper) {
        SimpleModule brobotModule = new SimpleModule("BrobotTestModule");

        // Handle OpenCV Mat - serialize as null, deserialize as null
        brobotModule.addSerializer(
                Mat.class,
                new JsonSerializer<Mat>() {
                    @Override
                    public void serialize(Mat value, JsonGenerator gen, SerializerProvider provider)
                            throws IOException {
                        gen.writeNull();
                    }
                });
        brobotModule.addDeserializer(
                Mat.class,
                new JsonDeserializer<Mat>() {
                    @Override
                    public Mat deserialize(JsonParser p, DeserializationContext ctxt)
                            throws IOException {
                        p.skipChildren();
                        return null;
                    }
                });

        // Handle BufferedImage - serialize as dimensions only
        brobotModule.addSerializer(
                BufferedImage.class,
                new JsonSerializer<BufferedImage>() {
                    @Override
                    public void serialize(
                            BufferedImage value, JsonGenerator gen, SerializerProvider provider)
                            throws IOException {
                        if (value != null) {
                            gen.writeStartObject();
                            gen.writeNumberField("width", value.getWidth());
                            gen.writeNumberField("height", value.getHeight());
                            gen.writeStringField("type", "BufferedImage");
                            gen.writeEndObject();
                        } else {
                            gen.writeNull();
                        }
                    }
                });
        brobotModule.addDeserializer(
                BufferedImage.class,
                new JsonDeserializer<BufferedImage>() {
                    @Override
                    public BufferedImage deserialize(JsonParser p, DeserializationContext ctxt)
                            throws IOException {
                        p.skipChildren();
                        // Return a dummy 1x1 image for tests
                        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
                    }
                });

        // Handle SikuliX Pattern
        brobotModule.addSerializer(
                Pattern.class,
                new JsonSerializer<Pattern>() {
                    @Override
                    public void serialize(
                            Pattern value, JsonGenerator gen, SerializerProvider provider)
                            throws IOException {
                        if (value != null) {
                            gen.writeStartObject();
                            gen.writeStringField("filename", value.getFilename());
                            gen.writeNumberField("similarity", value.getSimilar());
                            gen.writeEndObject();
                        } else {
                            gen.writeNull();
                        }
                    }
                });
        brobotModule.addDeserializer(
                Pattern.class,
                new JsonDeserializer<Pattern>() {
                    @Override
                    public Pattern deserialize(JsonParser p, DeserializationContext ctxt)
                            throws IOException {
                        p.skipChildren();
                        return new Pattern("test.png");
                    }
                });

        // Handle SikuliX Region
        brobotModule.addSerializer(
                Region.class,
                new JsonSerializer<Region>() {
                    @Override
                    public void serialize(
                            Region value, JsonGenerator gen, SerializerProvider provider)
                            throws IOException {
                        if (value != null) {
                            gen.writeStartObject();
                            gen.writeNumberField("x", value.x);
                            gen.writeNumberField("y", value.y);
                            gen.writeNumberField("w", value.w);
                            gen.writeNumberField("h", value.h);
                            gen.writeEndObject();
                        } else {
                            gen.writeNull();
                        }
                    }
                });
        brobotModule.addDeserializer(
                Region.class,
                new JsonDeserializer<Region>() {
                    @Override
                    public Region deserialize(JsonParser p, DeserializationContext ctxt)
                            throws IOException {
                        p.skipChildren();
                        return new Region(0, 0, 100, 100);
                    }
                });

        // Handle SikuliX Location
        brobotModule.addSerializer(
                Location.class,
                new JsonSerializer<Location>() {
                    @Override
                    public void serialize(
                            Location value, JsonGenerator gen, SerializerProvider provider)
                            throws IOException {
                        if (value != null) {
                            gen.writeStartObject();
                            gen.writeNumberField("x", value.x);
                            gen.writeNumberField("y", value.y);
                            gen.writeEndObject();
                        } else {
                            gen.writeNull();
                        }
                    }
                });
        brobotModule.addDeserializer(
                Location.class,
                new JsonDeserializer<Location>() {
                    @Override
                    public Location deserialize(JsonParser p, DeserializationContext ctxt)
                            throws IOException {
                        p.skipChildren();
                        return new Location(0, 0);
                    }
                });

        // Handle SikuliX Match
        brobotModule.addSerializer(
                Match.class,
                new JsonSerializer<Match>() {
                    @Override
                    public void serialize(
                            Match value, JsonGenerator gen, SerializerProvider provider)
                            throws IOException {
                        if (value != null) {
                            gen.writeStartObject();
                            gen.writeNumberField("x", value.x);
                            gen.writeNumberField("y", value.y);
                            gen.writeNumberField("w", value.w);
                            gen.writeNumberField("h", value.h);
                            gen.writeNumberField("score", value.getScore());
                            gen.writeEndObject();
                        } else {
                            gen.writeNull();
                        }
                    }
                });
        brobotModule.addDeserializer(
                Match.class,
                new JsonDeserializer<Match>() {
                    @Override
                    public Match deserialize(JsonParser p, DeserializationContext ctxt)
                            throws IOException {
                        p.skipChildren();
                        return new Match(new Region(0, 0, 100, 100), 0.95);
                    }
                });

        // Handle Screen
        brobotModule.addSerializer(
                Screen.class,
                new JsonSerializer<Screen>() {
                    @Override
                    public void serialize(
                            Screen value, JsonGenerator gen, SerializerProvider provider)
                            throws IOException {
                        if (value != null) {
                            gen.writeStartObject();
                            gen.writeNumberField("id", value.getID());
                            gen.writeEndObject();
                        } else {
                            gen.writeNull();
                        }
                    }
                });
        brobotModule.addDeserializer(
                Screen.class,
                new JsonDeserializer<Screen>() {
                    @Override
                    public Screen deserialize(JsonParser p, DeserializationContext ctxt)
                            throws IOException {
                        p.skipChildren();
                        return null; // Return null for tests, Screen requires display
                    }
                });

        mapper.registerModule(brobotModule);
    }

    /**
     * Creates a minimal ObjectMapper for simple serialization tests. This mapper only handles the
     * most basic cases.
     */
    public static ObjectMapper createMinimalTestMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
