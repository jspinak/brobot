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

/**
 * Writes Java code for methods in the StateTransitions class.
 */
@Component
@Getter
public class TransitionMethods {

    private final BabyStateRepo babyStateRepo;

    private Map<String, ClassName> enumNames;

    public TransitionMethods(BabyStateRepo babyStateRepo) {
        this.babyStateRepo = babyStateRepo;
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
        StringBuilder str = new StringBuilder();
        for (StateImageObject img : imgs) {
            String imgName = img.getAttributes().getImageName();
            String toTransition = getToTransitionStateName(img);
            Optional<ClassName> toEnumClassName =
                    getTransitionClassName(toTransition, packageName);
            if (toEnumClassName.isPresent()) { // if there is a transition
                String toEnumName = toTransition.toUpperCase();
                str.append(getTransitionAsString(stateClassVar, imgName, toEnumName));
                enumNames.put(toEnumName, toEnumClassName.get());
            }
        }
        return str.toString();
    }

    /* __example__
      .addTransition(() -> commonActions.click(1, fromState.getImage()), TO_STATE)
     */
    public String getTransitionAsString(String stateClassVar, String imageName, String targetEnumName) {
        String imageNameCap = Character.toUpperCase(imageName.charAt(0)) + imageName.substring(1);
        return "\n.addTransition(() -> commonActions.click(1, " + stateClassVar +
                ".get" + imageNameCap + "()), " + targetEnumName + ")";
    }

    /* __example__
      import static io.github.jspinak.brobot.manageStates.StateMemory.Enum.PREVIOUS;
     */
    public ClassName getPreviousClassName() {
        return ClassName.get("io.github.jspinak.brobot.manageStates.StateMemory","Enum");
    }

    private String getToTransitionStateName(StateImageObject image) {
        List<String> toTransitions = image.getAttributes().getTransitionsTo();
        if (toTransitions.isEmpty()) return "";
        return babyStateRepo.getTransitionStateName(toTransitions.get(0)); // find the full State name
    }

    public Optional<ClassName> getTransitionClassName(String toTransition, String packageName) {
        if (toTransition.equals("")) return Optional.empty(); // State doesn't exist
        if (toTransition.equals("previous")) return Optional.of(getPreviousClassName());
        String toTransitionCap; // make 1-char State names longer, and then simplify these lines
        if (toTransition.length() == 1) toTransitionCap = toTransition.toUpperCase();
        else toTransitionCap = Character.toUpperCase(toTransition.charAt(0)) + toTransition.substring(1);
        return Optional.of(ClassName.get(packageName+"."+toTransition+"."+toTransitionCap,"Name"));
    }

}
