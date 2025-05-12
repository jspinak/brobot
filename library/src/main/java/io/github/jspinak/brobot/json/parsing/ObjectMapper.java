package io.github.jspinak.brobot.json.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import io.github.jspinak.brobot.json.mixins.*;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ObjectMapper {

    private final com.fasterxml.jackson.databind.ObjectMapper mapper = configureObjectMapper();

    public ObjectMapper() {
        configureObjectMapper();
    }

    /**
     * Configure the ObjectMapper with standard settings
     */
    private com.fasterxml.jackson.databind.ObjectMapper configureObjectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule()); // For Java 8 date/time support

        // Add mixins to handle problematic classes
        mapper.addMixIn(Location.class, LocationMixin.class);
        mapper.addMixIn(org.bytedeco.opencv.opencv_core.Mat.class, MatMixin.class);
        mapper.addMixIn(Rect.class, JavaCVRectMixin.class);
        mapper.addMixIn(Rectangle.class, RectangleMixin.class);
        mapper.addMixIn(Rectangle2D.class, Rectangle2DMixin.class);
        mapper.addMixIn(org.sikuli.script.Location.class, SikuliLocationMixin.class);
        mapper.addMixIn(org.sikuli.script.Screen.class, SikuliScreenMixin.class);
        mapper.addMixIn(Region.class, RegionMixin.class);
        mapper.addMixIn(SearchRegions.class, SearchRegionsMixin.class);

        // Add mixins for image-related classes
        mapper.addMixIn(BufferedImage.class, BufferedImageMixin.class);
        mapper.addMixIn(Raster.class, RasterMixin.class);
        mapper.addMixIn(WritableRaster.class, WritableRasterMixin.class);
        mapper.addMixIn(ColorModel.class, ColorModelMixin.class);
        mapper.addMixIn(DataBuffer.class, DataBufferMixin.class);
        mapper.addMixIn(Image.class, BrobotImageMixin.class);
        mapper.addMixIn(Pattern.class, PatternMixin.class);

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
