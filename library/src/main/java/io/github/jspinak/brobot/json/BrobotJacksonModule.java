package io.github.jspinak.brobot.json;

import java.awt.image.BufferedImage;

import org.bytedeco.opencv.opencv_core.Mat;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.github.jspinak.brobot.json.serializers.*;

/**
 * Custom Jackson module for Brobot that registers all custom serializers and deserializers.
 *
 * <p>This module handles: - BufferedImage serialization (prevents native object serialization
 * errors) - OpenCV Mat serialization (prevents native pointer serialization errors) - Circular
 * reference handling through proper Jackson annotations
 *
 * <p>Usage:
 *
 * <pre>
 * ObjectMapper mapper = new ObjectMapper();
 * mapper.registerModule(new BrobotJacksonModule());
 * </pre>
 */
public class BrobotJacksonModule extends SimpleModule {

    private static final String MODULE_NAME = "BrobotJacksonModule";

    public BrobotJacksonModule() {
        super(MODULE_NAME, Version.unknownVersion());

        // Register BufferedImage serializers
        addSerializer(BufferedImage.class, new BufferedImageSerializer());
        addDeserializer(BufferedImage.class, new BufferedImageDeserializer());

        // Register OpenCV Mat serializers
        addSerializer(Mat.class, new MatSerializer());
        addDeserializer(Mat.class, new MatDeserializer());
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }
}
