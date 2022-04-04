package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.locationSpecs;

import com.squareup.javapoet.FieldSpec;
import io.github.jspinak.brobot.database.primitives.location.Location;
import io.github.jspinak.brobot.database.primitives.location.Position;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.database.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes Java code for a StateLocation.
 */
@Component
public class LocationFieldSpec {

    private List<Object> initializerArgs;

    public FieldSpec getCode(StateImageObject img) {
        initializerArgs = new ArrayList<>();
        String locationName = img.getAttributes().getImageName();
        Region sReg = img.getSearchRegion();
        String location = "\n\t.withLocation(new $T(new $T("+
                sReg.x+", "+sReg.y+", "+sReg.w+", "+sReg.h+"), $T.MIDDLEMIDDLE))";
        String fullBody = "new $T()\n\t" +
                ".called($S)" +
                location +
                "\n\t.build()";
        initializerArgs.add(StateLocation.Builder.class);
        initializerArgs.add(locationName);
        initializerArgs.add(Location.class);
        initializerArgs.add(Region.class);
        initializerArgs.add(Position.Name.class);
        return FieldSpec.builder(StateLocation.class, locationName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer(fullBody, initializerArgs.toArray())
                .build();
    }

}
