package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.stateSpecs;

import com.squareup.javapoet.*;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.regionSpecs.RegionFieldSpec;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.imageSpecs.ImageFieldSpec;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Writes Java code for the State class.
 */
@Component
public class WriteStateClass {

    private final ImageFieldSpec imageFieldSpec;
    private final RegionFieldSpec regionFieldSpec;
    private final StateFieldSpec stateFieldSpec;

    public WriteStateClass(ImageFieldSpec imageFieldSpec, RegionFieldSpec regionFieldSpec, StateFieldSpec stateFieldSpec) {
        this.imageFieldSpec = imageFieldSpec;
        this.regionFieldSpec = regionFieldSpec;
        this.stateFieldSpec = stateFieldSpec;
    }

    public JavaFile write(Set<StateImageObject> stateImages,
                          String enumName, String baseClassName, String packageName,
                          ClassName enumClass, String stateClassVar) {
        TypeSpec enumType = stateFieldSpec.getEnum(enumName);
        MethodSpec stateService = stateFieldSpec.saveState();
        List<FieldSpec> stateObjects = new ArrayList<>();
        List<String> imageNames = new ArrayList<>();
        List<String> regionNames = new ArrayList<>();
        stateImages.forEach(img -> {
            if (img.getAttributes().isStateRegion()) {
                stateObjects.add(regionFieldSpec.getCode(img));
                regionNames.add(img.getAttributes().getImageName());
            }
            if (img.getAttributes().isStateImage()) {
                stateObjects.add(imageFieldSpec.getCode(img));
                imageNames.add(img.getAttributes().getImageName());
            }
        });
        FieldSpec state = stateFieldSpec.getStateField(imageNames, regionNames, enumName);

        TypeSpec stateClass = TypeSpec.classBuilder(baseClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class)
                .addAnnotation(Getter.class)
                .addType(enumType)
                .addFields(stateObjects)
                .addField(state)
                .addMethod(stateService)
                .build();

        return JavaFile.builder(packageName+"."+stateClassVar, stateClass)
                .addStaticImport(enumClass, enumName)
                .build();
    }
}
