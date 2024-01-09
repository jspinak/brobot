package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;
import org.apache.pdfbox.debugger.ui.MapEntry;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Getter
public class TempStateRepo {

    private List<TempStateWithImages> states = new ArrayList<>();

    private void addState(TempStateWithImages tempStateWithImages) {
        this.states.add(tempStateWithImages);
    }

    public void addImage(StateImage stateImage, List<SceneCombination> sceneCombinations) {
        if (sceneCombinations.isEmpty()) return;
        Set<Integer> scenes = new HashSet<>();
        for (SceneCombination sceneCombination : sceneCombinations) {
            if (sceneCombination.contains(stateImage)) {
                scenes.add(sceneCombination.getScene1());
                scenes.add(sceneCombination.getScene2());
            }
        }
        TempStateWithImages state = getState(scenes);
        state.addImage(stateImage);
    }

    public void addImage(StateImage stateImage, Set<Integer> imageInTheseScenes) {
        TempStateWithImages state = getState(imageInTheseScenes);
        stateImage.setOwnerStateName(state.getName());
        state.addImage(stateImage);
    }

    private TempStateWithImages getState(Set<Integer> scenes) {
        // if the state exists, return the state
        for (TempStateWithImages state : states) {
            if (state.hasEqualSceneSets(scenes)) return state;
        }
        // otherwise, create the state and add it to the repo
        StringBuilder name = new StringBuilder();
        scenes.forEach(scene -> name.append(scene).append("-"));
        TempStateWithImages state = new TempStateWithImages(name.toString());
        states.add(state);
        return state;
    }

    public List<StateImage> getAllStateImages() {
        List<StateImage> allImages = new ArrayList<>();
        states.forEach(state -> allImages.addAll(state.getImages()));
        return allImages;
    }


}
