package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.imageSpecs;

import com.squareup.javapoet.FieldSpec;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes.Attribute.SINGLE_MATCH;

/**
 * Writes Java code for a StateImage
 */
@Component
public class ImageFieldSpec {

    private SnapshotsAsCode snapshotsAsCode;

    private List<Object> initializerArgs;

    public ImageFieldSpec(SnapshotsAsCode snapshotsAsCode) {
        this.snapshotsAsCode = snapshotsAsCode;
    }

    public FieldSpec getCode(StateImage img) {
        initializerArgs = new ArrayList<>();
        String imageName = img.getAttributes().getImageName();
        snapshotsAsCode.processSnapshots(img);
        String fixed = img.isFixed() ? "" : "\n\t.isFixed(false)";
        String shared = img.getAttributeData(SINGLE_MATCH).getPageResults().containsValue(false)?
                "\n\t.isShared(true)" : "";
        Region sReg = img.getSearchRegion();
        String searchRegion = !sReg.defined()? "" : "\n\t.withSearchRegion("+
                sReg.x+", "+sReg.y+", "+sReg.w+", "+sReg.h+")";
        String fullBody = "new $T()" +
                "\n\t.called($S)" +
                img.getAttributes().getWithImagesLineInBuilder() +
                fixed +
                shared +
                searchRegion +
                snapshotsAsCode.getBody() +
                "\n\t.build()";
        initializerArgs.add(StateImage.Builder.class);
        initializerArgs.add(imageName);
        initializerArgs.addAll(img.getAttributes().getFilenames());
        if (snapshotsAsCode.isAddClass()) {
            initializerArgs.add(MatchSnapshot.Builder.class);
            initializerArgs.add(ActionOptions.Find.class);
        }
        return FieldSpec.builder(StateImage.class, imageName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer(fullBody, initializerArgs.toArray())
                .build();
    }

}
