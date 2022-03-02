package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.transitionSpecs;

import com.squareup.javapoet.ClassName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToTransition {

    private String toTransitionStateName;
    private String toEnumName;
    private String toTransitionFolder;
    private ClassName enumClassName;
    private String packageName;

    public ToTransition(String packageName, String toTransitionStateName) {
        this.packageName = packageName;
        this.toTransitionStateName = toTransitionStateName;
        this.toEnumName = toTransitionStateName.toUpperCase();
        setToTransitionFolder(toTransitionStateName);
        setTransitionClassName();
    }

    /* __example__
      import static io.github.jspinak.brobot.manageStates.StateMemory.Enum.PREVIOUS;
     */
    public ClassName getPreviousClassName() {
        return ClassName.get("io.github.jspinak.brobot.manageStates.StateMemory","Enum");
    }

    private void setToTransitionFolder(String toState) {
        if (toState.isEmpty()) return;
        if (toState.length() == 1) {
            toTransitionFolder = toState.toLowerCase();
            return;
        }
        toTransitionFolder = Character.toLowerCase(toState.charAt(0)) + toState.substring(1);
    }

    public void setTransitionClassName() {
        if (toTransitionStateName.equals("previous")) {
            enumClassName = getPreviousClassName();
            return;
        }
        String toTransitionCap; // you should make 1-char State names longer, and then simplify these lines
        if (toTransitionStateName.length() == 1) toTransitionCap = toTransitionStateName.toUpperCase();
        else toTransitionCap = Character.toUpperCase(toTransitionStateName.charAt(0))
                + toTransitionStateName.substring(1);
        enumClassName = ClassName.get(
                packageName +"."+ toTransitionFolder +"."+ toTransitionCap, "Name");
    }

}
