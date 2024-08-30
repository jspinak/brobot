package io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.babyStates;

import io.github.jspinak.brobot.app.stateStructureBuilders.ExtendedStateImageDTO;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.attributes.AttributeTypes;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class BabyState {

    private String name;
    private Set<ExtendedStateImageDTO> images = new HashSet<>();

    public BabyState(String name, ExtendedStateImageDTO image) {
        this.name = name;
        addImage(image);
    }

    public void addImage(ExtendedStateImageDTO stateImage) {
        images.add(stateImage);
    }

    public List<ExtendedStateImageDTO> getImagesByAttributeAndPage(AttributeTypes.Attribute attribute, int page) {
        return images.stream()
                .filter(image -> image.getAttributes().getActiveAttributes(page).contains(attribute))
                .collect(Collectors.toList());
    }

}
