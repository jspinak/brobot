package io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates;

import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<StateImageObject> getImagesByAttributeAndPage(AttributeTypes.Attribute attribute, int page) {
        return images.stream()
                .filter(image -> image.getAttributes().getActiveAttributes(page).contains(attribute))
                .collect(Collectors.toList());
    }

}
