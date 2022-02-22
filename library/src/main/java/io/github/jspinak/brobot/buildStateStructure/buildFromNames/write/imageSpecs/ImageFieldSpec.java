package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.imageSpecs;

import com.squareup.javapoet.FieldSpec;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.database.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes Java code for a StateImageObject
 */
@Component
public class ImageFieldSpec {

    private SnapshotsAsCode snapshotsAsCode;

    private List<Object> initializerArgs;

    public ImageFieldSpec(SnapshotsAsCode snapshotsAsCode) {
        this.snapshotsAsCode = snapshotsAsCode;
    }

    public FieldSpec getCode(StateImageObject img) {
        initializerArgs = new ArrayList<>();
        String imageName = img.getAttributes().getImageName();
        snapshotsAsCode.processSnapshots(img);
        String fixed = img.isFixed() ? "" : "\n.isFixed(false)";
        Region sReg = img.getSearchRegion();
        String searchRegion = !sReg.defined()? "" : "\n.withSearchRegion("+
                sReg.x+", "+sReg.y+", "+sReg.w+", "+sReg.h+")";
        String fullBody = "new $T()\n" +
                ".withImages($S)" +
                fixed +
                searchRegion +
                snapshotsAsCode.getBody() +
                "\n.build()";
        initializerArgs.add(StateImageObject.Builder.class);
        initializerArgs.add(img.getName());
        if (snapshotsAsCode.isAddClass()) {
            initializerArgs.add(MatchSnapshot.Builder.class);
            initializerArgs.add(ActionOptions.Find.class);
        }
        return FieldSpec.builder(StateImageObject.class, imageName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer(fullBody, initializerArgs.toArray())
                .build();
    }

}
