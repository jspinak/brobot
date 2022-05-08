package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.regionSpecs;

import com.squareup.javapoet.FieldSpec;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes Java code for a StateRegion.
 */
@Component
public class RegionFieldSpec {

    private List<Object> initializerArgs;

    public FieldSpec getCode(StateImageObject img) {
        initializerArgs = new ArrayList<>();
        String imageName = img.getAttributes().getImageName();
        Region sReg = img.getSearchRegion();
        String searchRegion = !sReg.defined()? "" : "\n.withSearchRegion("+
                sReg.x+", "+sReg.y+", "+sReg.w+", "+sReg.h+")";
        String fullBody = "new $T()\n" +
                ".called($S)" +
                searchRegion +
                "\n.build()";
        initializerArgs.add(StateRegion.Builder.class);
        initializerArgs.add(imageName);
        return FieldSpec.builder(StateRegion.class, imageName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer(fullBody, initializerArgs.toArray())
                .build();
    }

}
