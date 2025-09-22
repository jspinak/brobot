package io.github.jspinak.brobot.runner.json.parsing;

import java.io.IOException;

import org.bytedeco.opencv.opencv_core.Mat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.runner.json.module.BrobotJsonModule;
import io.github.jspinak.brobot.runner.json.serializers.*;

/**
 * Test implementation of BrobotJsonModule with minimal serializers. Used for testing
 * BrobotObjectMapper configuration without complex dependencies.
 */
public class TestBrobotJsonModule extends BrobotJsonModule {

    public TestBrobotJsonModule() {
        super(
                createMatchesSerializer(),
                createObjectCollectionSerializer(),
                createMatSerializer(),
                createImageSerializer(),
                createActionConfigDeserializer(),
                createImageDeserializer(),
                createSearchRegionsDeserializer(),
                createMatDeserializer());
    }

    private static MatchesSerializer createMatchesSerializer() {
        return new MatchesSerializer() {
            @Override
            public void serialize(
                    ActionResult value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                gen.writeStartObject();
                gen.writeBooleanField("success", value.isSuccess());
                gen.writeEndObject();
            }
        };
    }

    private static ObjectCollectionSerializer createObjectCollectionSerializer() {
        return new ObjectCollectionSerializer() {
            @Override
            public void serialize(
                    ObjectCollection value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                gen.writeStartObject();
                gen.writeNumberField("imageCount", value.getStateImages().size());

                // Serialize scenes array
                gen.writeArrayFieldStart("scenes");
                for (Scene scene : value.getScenes()) {
                    gen.writeStartObject();
                    if (scene.getPattern() != null && scene.getPattern().getNameWithoutExtension() != null) {
                        gen.writeStringField("name", scene.getPattern().getNameWithoutExtension());
                    }
                    gen.writeEndObject();
                }
                gen.writeEndArray();

                gen.writeEndObject();
            }
        };
    }

    private static MatSerializer createMatSerializer() {
        return new MatSerializer() {
            @Override
            public void serialize(Mat value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                gen.writeStartObject();
                if (value != null) {
                    gen.writeNumberField("rows", value.rows());
                    gen.writeNumberField("cols", value.cols());
                }
                gen.writeEndObject();
            }
        };
    }

    private static ImageSerializer createImageSerializer() {
        return new ImageSerializer() {
            @Override
            public void serialize(Image value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                gen.writeStartObject();
                if (value.getName() != null) {
                    gen.writeStringField("name", value.getName());
                }
                // Only serialize metadata, exclude binary data
                // Do not serialize bufferedImage, matBGR, matHSV, or sikuli fields
                gen.writeEndObject();
            }
        };
    }

    private static ActionConfigDeserializer createActionConfigDeserializer() {
        return new ActionConfigDeserializer() {
            @Override
            public ActionConfig deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                return null; // Minimal implementation for testing
            }
        };
    }

    private static ImageDeserializer createImageDeserializer() {
        return new ImageDeserializer() {
            @Override
            public Image deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new Image("test.png"); // Minimal implementation
            }
        };
    }

    private static SearchRegionsDeserializer createSearchRegionsDeserializer() {
        return new SearchRegionsDeserializer() {
            @Override
            public SearchRegions deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                return new SearchRegions(); // Minimal implementation
            }
        };
    }

    private static MatDeserializer createMatDeserializer() {
        return new MatDeserializer() {
            @Override
            public Mat deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new Mat(); // Minimal implementation
            }
        };
    }
}
