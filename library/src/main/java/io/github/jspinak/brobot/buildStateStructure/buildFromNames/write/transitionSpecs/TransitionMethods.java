package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.transitionSpecs;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import io.github.jspinak.brobot.actions.customActions.CommonActions;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates.BabyStateRepo;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes.Tag.TRANSITION;

/**
 * Writes Java code for methods in the StateTransitions class.
 */
@Component
@Getter
public class TransitionMethods {

    private final BabyStateRepo babyStateRepo;
    private final ToTransitions toTransitions;

    private Map<String, ClassName> enumNames;

    public TransitionMethods(BabyStateRepo babyStateRepo, ToTransitions toTransitions) {
        this.babyStateRepo = babyStateRepo;
        this.toTransitions = toTransitions;
    }

    /* __example__
    private boolean finishTransition() {
      return commonActions.findState(1, EXAMPLESTATE);
    }
     */
    public MethodSpec getFinishTransition(String enumName) {
        return MethodSpec.methodBuilder("finishTransition")
                .returns(boolean.class)
                .addModifiers(Modifier.PRIVATE)
                .addStatement("return commonActions.findState(1, $L)", enumName)
                .build();
    }

    /* __example__
    public ExampleStateTransitions(CommonActions commonActions, ExampleState exampleState,
                                   StateTransitionsRepository stateTransitionsRepository) {
        this.commonActions = commonActions;
        this.exampleState = exampleState;
        stateTransitionsRepository.add(
          new StateTransitions.Builder(EXAMPLESTATE)
          .addTransitionFinish(this::finishTransition)
          .build());
     }

     All enums can be added as $L instead of a $T since the import statement must be
     included as a 'static' import in the JavaFile.Builder.
     */
    public MethodSpec getConstructor(ClassName className, String stateClassVar, String enumName,
                                     MethodSpec finishTransition, String packageName,
                                     Set<StateImageObject> imgs) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CommonActions.class, "commonActions")
                .addParameter(className, stateClassVar)
                .addParameter(StateTransitionsRepository.class, "stateTransitionsRepository")
                .addStatement("this.commonActions = commonActions")
                .addStatement("this.$L = $L", stateClassVar, stateClassVar)
                .addStatement("stateTransitionsRepository.add(" +
                        "\nnew $T($L)" +
                        "\n.addTransitionFinish(this::$N)" +
                        getTransitionsAsString(imgs, stateClassVar, packageName) +
                        "\n.build())", StateTransitions.Builder.class, enumName, finishTransition)
                .build();
    }

    /*
    Gets the Transitions to other States as a String, for use in the getConstructor method.
    Adds target enums to the list of enums. These enums will be added later as static imports.
     */
    private String getTransitionsAsString(Set<StateImageObject> imgs, String stateClassVar,
                                          String packageName) {
        enumNames = new HashMap<>();
        StringBuilder transitionBuilder = new StringBuilder();
        Set<StateImageObject> imgsWithTransitions = imgs.stream()
                        .filter(img -> !img.getAttributes().getTransitionsTo().isEmpty())
                        .collect(Collectors.toSet());
        for (StateImageObject img : imgsWithTransitions) {
            toTransitions.setTransitions(img, packageName);
            transitionBuilder.append(toTransitions.getTransitionCode(stateClassVar));
            enumNames.putAll(toTransitions.getEnumNames());
        }
        return transitionBuilder.toString();
    }

}
