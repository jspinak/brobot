package io.github.jspinak.brobot.json.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.json.serializers.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

@Component
public class BrobotJsonModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public BrobotJsonModule(
            ActionOptionsSerializer actionOptionsSerializer,
            MatchesSerializer matchesSerializer,
            ObjectCollectionSerializer objectCollectionSerializer,
            MatSerializer matSerializer,
            ImageSerializer imageSerializer,
            ImageDeserializer imageDeserializer,
            SearchRegionsDeserializer searchRegionsDeserializer
    ) {
        super("BrobotJsonModule");
        configure(
                actionOptionsSerializer,
                matchesSerializer,
                objectCollectionSerializer,
                matSerializer,
                imageSerializer,
                imageDeserializer,
                searchRegionsDeserializer
        );
    }

    private void configure(
            ActionOptionsSerializer actionOptionsSerializer,
            MatchesSerializer matchesSerializer,
            ObjectCollectionSerializer objectCollectionSerializer,
            MatSerializer matSerializer,
            ImageSerializer imageSerializer,
            ImageDeserializer imageDeserializer,
            SearchRegionsDeserializer searchRegionsDeserializer
    ) {
        // Register serializers
        addSerializer(ActionOptions.class, actionOptionsSerializer);
        addSerializer(Matches.class, matchesSerializer);
        addSerializer(ObjectCollection.class, objectCollectionSerializer);
        addSerializer(Mat.class, matSerializer);
        addSerializer(Image.class, imageSerializer);

        // Register deserializers
        addDeserializer(Image.class, imageDeserializer);
        addDeserializer(SearchRegions.class, searchRegionsDeserializer);
    }
}