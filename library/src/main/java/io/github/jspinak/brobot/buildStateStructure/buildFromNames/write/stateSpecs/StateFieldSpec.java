package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.stateSpecs;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.jspinak.brobot.database.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.services.StateService;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * Writes Java code for the remaining components of a State class:
 * - the State variable (State state = new State.Builder(ENUM)...)
 * - the initialization of this variable 'stateService.save(state)'.
 * - the Enum declaration 'public enum Name implements StateEnum...'.
 */
@Component
public class StateFieldSpec {

    private StateBuilder stateBuilder;

    public StateFieldSpec(StateBuilder stateBuilder) {
        this.stateBuilder = stateBuilder;
    }

    public TypeSpec getEnum(String enumName) {
        return TypeSpec.enumBuilder("Name")
                .addModifiers(Modifier.PUBLIC)
                .addEnumConstant(enumName)
                .addSuperinterface(StateEnum.class)
                .build();
    }

    public MethodSpec saveState() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(StateService.class, "stateService")
                .addStatement("stateService.save(state)")
                .build();
    }

    public FieldSpec getStateField(List<String> imageNames, List<String> regionNames, String enumName) {
        return FieldSpec.builder(State.class, "state")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer(stateBuilder.getCode(imageNames, regionNames), State.Builder.class, enumName)
                .build();
    }
}
