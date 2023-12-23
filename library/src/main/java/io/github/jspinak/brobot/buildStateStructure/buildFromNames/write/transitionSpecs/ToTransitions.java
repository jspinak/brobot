package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.transitionSpecs;

import com.squareup.javapoet.ClassName;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates.BabyStateRepo;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Component
public class ToTransitions {

    private BabyStateRepo babyStateRepo;

    private String imgName;
    private List<ToTransition> transitions = new ArrayList<>();
    private Map<String, ClassName> enumNames;

    public ToTransitions(BabyStateRepo babyStateRepo) {
        this.babyStateRepo = babyStateRepo;
    }

    public void setTransitions(StateImage img, String packageName) {
        transitions = new ArrayList<>();
        enumNames = new HashMap<>();
        imgName = img.getAttributes().getImageName();
        img.getAttributes().getTransitionsTo().forEach(to -> {
            String toState = babyStateRepo.getTransitionStateName(to);
            if (!toState.isEmpty()) transitions.add(
                    new ToTransition(packageName, toState));
        });
        transitions.forEach(tr -> enumNames.put(tr.getToEnumName(), tr.getEnumClassName()));
    }

    /* __example__
      .addTransition(() -> commonActions.click(1, fromState.getImage()), TO_STATE, TO_STATE2)
    */
    public String getTransitionCode(String stateClassVar) {
        String imageNameCap = Character.toUpperCase(imgName.charAt(0)) + imgName.substring(1);
        StringBuilder enums = new StringBuilder();
        transitions.forEach(tr -> enums.append(tr.getToEnumName()));
        return "\n.addTransition(() -> commonActions.click(1, " + stateClassVar +
                ".get" + imageNameCap + "()), " + enums + ")";
    }
}
