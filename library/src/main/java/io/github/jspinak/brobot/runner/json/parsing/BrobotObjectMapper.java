package io.github.jspinak.brobot.runner.json.parsing;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.github.jspinak.brobot.json.BrobotJacksonModule;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.runner.json.mixins.*;
import io.github.jspinak.brobot.runner.json.module.BrobotJsonModule;

/**
 * A wrapper around Jackson's ObjectMapper configured specifically for Brobot JSON operations.
 *
 * <p>This class provides a centralized configuration for JSON processing throughout the Brobot
 * framework. It wraps Jackson's ObjectMapper with Brobot-specific configurations including:
 *
 * <ul>
 *   <li>Custom serializers and deserializers via {@link BrobotJsonModule}
 *   <li>Mixins for problematic third-party classes (Sikuli, OpenCV, AWT)
 *   <li>Relaxed parsing settings for flexibility
 *   <li>Java 8 time API support
 *   <li>Pretty printing enabled by default
 * </ul>
 *
 * <p>The wrapper pattern is used to:
 *
 * <ul>
 *   <li>Avoid naming conflicts with Jackson's ObjectMapper
 *   <li>Provide a Spring-managed singleton for consistent JSON processing
 *   <li>Centralize mixin registration and configuration
 *   <li>Expose only the methods needed by Brobot
 * </ul>
 *
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see BrobotJsonModule
 * @see JsonParser
 */
@Component
public class BrobotObjectMapper {

    private final com.fasterxml.jackson.databind.ObjectMapper mapper;

    @Autowired
    public BrobotObjectMapper(BrobotJsonModule brobotJsonModule) {
        this.mapper = configureObjectMapper(brobotJsonModule);
    }

    /**
     * Configures the Jackson ObjectMapper with Brobot-specific settings and modules.
     *
     * <p>Configuration includes:
     *
     * <ul>
     *   <li>Lenient deserialization - ignores unknown properties
     *   <li>Pretty printing - enables indented output
     *   <li>Java time support - handles LocalDateTime, Duration, etc.
     *   <li>Comment support - allows comments in JSON files
     *   <li>Circular reference handling - prevents StackOverflowError
     *   <li>Custom module - registers Brobot-specific serializers/deserializers
     *   <li>Mixins - handles problematic third-party classes
     * </ul>
     *
     * <p>Mixins are registered for:
     *
     * <ul>
     *   <li>OpenCV classes: Mat, Rect
     *   <li>AWT classes: Rectangle, Rectangle2D, BufferedImage, Raster, etc.
     *   <li>Sikuli classes: Location, Screen
     *   <li>Brobot classes: Region, SearchRegions, Match, Image
     * </ul>
     *
     * @param brobotJsonModule The module containing custom serializers and deserializers
     * @return A fully configured ObjectMapper instance
     */
    private com.fasterxml.jackson.databind.ObjectMapper configureObjectMapper(
            BrobotJsonModule brobotJsonModule) {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule()); // For Java 8 date/time support
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        // Register the BrobotJsonModule for custom serializers
        mapper.registerModule(brobotJsonModule);

        // Register our custom serializers for BufferedImage and Mat
        mapper.registerModule(new BrobotJacksonModule());

        // Add mixins to handle problematic classes
        mapper.addMixIn(org.bytedeco.opencv.opencv_core.Mat.class, MatMixin.class);
        mapper.addMixIn(Rect.class, JavaCVRectMixin.class);
        mapper.addMixIn(Rectangle.class, RectangleMixin.class);
        mapper.addMixIn(Rectangle2D.class, Rectangle2DMixin.class);
        mapper.addMixIn(org.sikuli.script.Location.class, SikuliLocationMixin.class);
        mapper.addMixIn(org.sikuli.script.Screen.class, SikuliScreenMixin.class);
        mapper.addMixIn(Region.class, RegionMixin.class);
        mapper.addMixIn(SearchRegions.class, SearchRegionsMixin.class);
        mapper.addMixIn(io.github.jspinak.brobot.model.match.Match.class, MatchMixin.class);

        // Add mixins for image-related classes
        mapper.addMixIn(BufferedImage.class, BufferedImageMixin.class);
        mapper.addMixIn(Raster.class, RasterMixin.class);
        mapper.addMixIn(WritableRaster.class, WritableRasterMixin.class);
        mapper.addMixIn(ColorModel.class, ColorModelMixin.class);
        mapper.addMixIn(DataBuffer.class, DataBufferMixin.class);
        mapper.addMixIn(io.github.jspinak.brobot.model.element.Image.class, BrobotImageMixin.class);

        return mapper;
    }

    public JsonNode readTree(String json) throws JsonProcessingException {
        return mapper.readTree(json);
    }

    public JsonNode readTree(File file) throws IOException {
        return mapper.readTree(file);
    }

    public JsonNode readTree(InputStream inputStream) throws IOException {
        return mapper.readTree(inputStream);
    }

    public <T> T treeToValue(JsonNode node, Class<T> valueType) throws JsonProcessingException {
        return mapper.treeToValue(node, valueType);
    }

    public <T> T readValue(String content, Class<T> valueType) throws JsonProcessingException {
        return mapper.readValue(content, valueType);
    }

    public String writeValueAsString(Object value) throws JsonProcessingException {
        return mapper.writeValueAsString(value);
    }

    public ObjectWriter writerWithDefaultPrettyPrinter() {
        return mapper.writerWithDefaultPrettyPrinter();
    }

    public ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }
}
