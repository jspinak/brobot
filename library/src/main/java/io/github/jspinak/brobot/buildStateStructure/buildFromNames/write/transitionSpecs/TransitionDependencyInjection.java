package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.transitionSpecs;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import io.github.jspinak.brobot.actions.customActions.CommonActions;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;

/**
 * Writes variable declarations for classes referenced with dependency injection.
 * Used for the StateTransitions class.
 */
@Component
public class TransitionDependencyInjection {

    // private final CommonActions commonActions;
    public FieldSpec getCommonActions() {
        return FieldSpec.builder(CommonActions.class, "commonActions")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    // private final InjectedClass injectedClass;
    public FieldSpec getInjectedClass(String stateClassVar, ClassName className) {
        return FieldSpec.builder(className, stateClassVar)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }


}
