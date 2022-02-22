package io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates;

import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class BabyState {

    private String name;
    private Set<StateImageObject> images = new HashSet<>();

    public BabyState(String name, StateImageObject image) {
        this.name = name;
        addImage(image);
    }

    public void addImage(StateImageObject stateImageObject) {
        images.add(stateImageObject);
    }

}
