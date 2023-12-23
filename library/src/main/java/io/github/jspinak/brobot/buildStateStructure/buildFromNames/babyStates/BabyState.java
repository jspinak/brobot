package io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates;

import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class BabyState {

    private String name;
    private Set<StateImage> images = new HashSet<>();

    public BabyState(String name, StateImage image) {
        this.name = name;
        addImage(image);
    }

    public void addImage(StateImage stateImage) {
        images.add(stateImage);
    }

    public List<StateImage> getImagesByAttributeAndPage(AttributeTypes.Attribute attribute, int page) {
        return images.stream()
                .filter(image -> image.getAttributes().getActiveAttributes(page).contains(attribute))
                .collect(Collectors.toList());
    }

}
