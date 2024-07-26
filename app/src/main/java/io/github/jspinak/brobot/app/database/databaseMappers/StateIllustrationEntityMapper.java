package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateIllustrationEntity;
import io.github.jspinak.brobot.app.web.responses.StateIllustrationResponse;
import io.github.jspinak.brobot.illustratedHistory.StateIllustration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StateIllustrationEntityMapper {

    private final ImageEntityMapper imageEntityMapper;

    StateIllustrationEntityMapper(ImageEntityMapper imageEntityMapper) {
        this.imageEntityMapper = imageEntityMapper;
    }

    public StateIllustrationEntity map(StateIllustration stateIllustration) {
        StateIllustrationEntity stateIllustrationEntity = new StateIllustrationEntity();
        stateIllustrationEntity.setIllustratedScreenshot(imageEntityMapper.map(stateIllustration.getIllustratedScreenshot()));
        return stateIllustrationEntity;
    }

    public StateIllustration map(StateIllustrationEntity stateIllustrationEntity) {
        StateIllustration stateIllustration = new StateIllustration();
        stateIllustration.setIllustratedScreenshot(imageEntityMapper.map(stateIllustrationEntity.getIllustratedScreenshot()));
        return stateIllustration;
    }

    public List<StateIllustrationEntity> mapToListEntity(List<StateIllustration> stateIllustrations) {
        if (stateIllustrations == null) {
            return null;
        }
        return stateIllustrations.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public List<StateIllustration> mapToList(List<StateIllustrationEntity> stateIllustrationEntities) {
        if (stateIllustrationEntities == null) {
            return null;
        }
        return stateIllustrationEntities.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

}
