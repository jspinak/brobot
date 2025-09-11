package io.github.jspinak.brobot.runner.json.module;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.module.SimpleModule;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.runner.json.serializers.*;

/**
 * Jackson module that configures custom serializers and deserializers for Brobot domain objects.
 *
 * <p>This module is the central configuration point for JSON processing of Brobot-specific classes
 * and third-party library objects that require special handling. It registers custom serializers
 * and deserializers to ensure proper JSON representation while avoiding common pitfalls like
 * circular references, native memory structures, and heavyweight objects.
 *
 * <p>Registered serializers:
 *
 * <ul>
 *   <li>{@link ActionConfig} - Handles polymorphic action configuration with proper handling of
 *       enums and nested objects
 *   <li>{@link ActionResult} - Serializes match results while avoiding circular references
 *   <li>{@link ObjectCollection} - Handles collections of Brobot objects with state management
 *   <li>{@link Mat} - Converts OpenCV Mat objects to/from base64 encoded strings
 *   <li>{@link Image} - Serializes Brobot images with efficient handling of pixel data
 * </ul>
 *
 * <p>Registered deserializers:
 *
 * <ul>
 *   <li>{@link ActionConfig} - Deserializes polymorphic action configuration objects
 *   <li>{@link Image} - Reconstructs Image objects from JSON with proper resource loading
 *   <li>{@link SearchRegions} - Deserializes search region configurations
 * </ul>
 *
 * <p>This module works in conjunction with the mixin classes in {@code
 * io.github.jspinak.brobot.json.mixins} to provide comprehensive JSON support for the entire Brobot
 * object model.
 *
 * @see io.github.jspinak.brobot.runner.json.config.JsonConfiguration
 * @see com.fasterxml.jackson.databind.module.SimpleModule
 */
@Component
public class BrobotJsonModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs the BrobotJsonModule with all required serializers and deserializers.
     *
     * <p>All serializers and deserializers are injected via Spring dependency injection, ensuring
     * they are properly configured and ready for use.
     *
     * @param matchesSerializer Handles serialization of action results and match objects
     * @param objectCollectionSerializer Handles serialization of object collections with state
     *     references
     * @param matSerializer Handles serialization of OpenCV Mat objects to base64
     * @param imageSerializer Handles serialization of Brobot Image objects
     * @param actionConfigDeserializer Handles deserialization of polymorphic action configuration
     *     objects
     * @param imageDeserializer Handles deserialization of Brobot Image objects
     * @param searchRegionsDeserializer Handles deserialization of search region configurations
     * @param matDeserializer Handles deserialization of OpenCV Mat objects
     */
    public BrobotJsonModule(
            MatchesSerializer matchesSerializer,
            ObjectCollectionSerializer objectCollectionSerializer,
            MatSerializer matSerializer,
            ImageSerializer imageSerializer,
            ActionConfigDeserializer actionConfigDeserializer,
            ImageDeserializer imageDeserializer,
            SearchRegionsDeserializer searchRegionsDeserializer,
            MatDeserializer matDeserializer) {
        super("BrobotJsonModule");
        configure(
                matchesSerializer,
                objectCollectionSerializer,
                matSerializer,
                imageSerializer,
                actionConfigDeserializer,
                imageDeserializer,
                searchRegionsDeserializer,
                matDeserializer);
    }

    /**
     * Configures the module by registering all custom serializers and deserializers.
     *
     * <p>This method sets up the type mappings between Brobot domain classes and their
     * corresponding JSON handlers. The registration order is not significant as Jackson will look
     * up the appropriate handler based on the exact class type during
     * serialization/deserialization.
     *
     * @param matchesSerializer Serializer for ActionResult objects containing matches
     * @param objectCollectionSerializer Serializer for collections of Brobot objects
     * @param matSerializer Serializer for OpenCV Mat objects
     * @param imageSerializer Serializer for Brobot Image objects
     * @param actionConfigDeserializer Deserializer for polymorphic ActionConfig objects
     * @param imageDeserializer Deserializer for Brobot Image objects
     * @param searchRegionsDeserializer Deserializer for SearchRegions configurations
     * @param matDeserializer Deserializer for OpenCV Mat objects
     */
    private void configure(
            MatchesSerializer matchesSerializer,
            ObjectCollectionSerializer objectCollectionSerializer,
            MatSerializer matSerializer,
            ImageSerializer imageSerializer,
            ActionConfigDeserializer actionConfigDeserializer,
            ImageDeserializer imageDeserializer,
            SearchRegionsDeserializer searchRegionsDeserializer,
            MatDeserializer matDeserializer) {
        // Register serializers
        addSerializer(ActionResult.class, matchesSerializer);
        addSerializer(ObjectCollection.class, objectCollectionSerializer);
        addSerializer(Mat.class, matSerializer);
        addSerializer(Image.class, imageSerializer);

        // Register deserializers
        addDeserializer(ActionConfig.class, actionConfigDeserializer);
        addDeserializer(Image.class, imageDeserializer);
        addDeserializer(SearchRegions.class, searchRegionsDeserializer);
        addDeserializer(Mat.class, matDeserializer);
    }
}
