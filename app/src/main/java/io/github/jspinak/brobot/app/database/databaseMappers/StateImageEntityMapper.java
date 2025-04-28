package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.services.PatternService;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class StateImageEntityMapper {
    
    public StateImageEntity map(StateImage stateImage, PatternService patternService) {
        StateImageEntity stateImageEntity = new StateImageEntity();
        stateImageEntity.setProjectId(stateImage.getProjectId());
        stateImageEntity.setObjectType(stateImage.getObjectType());
        stateImageEntity.setName(stateImage.getName());
        stateImageEntity.setPatterns(patternService.map(stateImage.getPatterns()));
        stateImageEntity.setOwnerStateName(stateImage.getOwnerStateName());
        stateImageEntity.setTimesActedOn(stateImage.getTimesActedOn());
        stateImageEntity.setShared(stateImageEntity.isShared());
        stateImageEntity.setIndex(stateImage.getIndex());
        stateImageEntity.setDynamic(stateImage.isDynamic());
        stateImageEntity.setInvolvedTransitionIds(stateImage.getInvolvedTransitionIds());
        return stateImageEntity;
    }

    public StateImage map(StateImageEntity stateImageEntity, PatternService patternService) {
        StateImage stateImage = new StateImage();
        stateImage.setId(stateImageEntity.getId());
        stateImage.setProjectId(stateImageEntity.getProjectId());
        stateImage.setObjectType(stateImageEntity.getObjectType());
        stateImage.setName(stateImageEntity.getName());
        stateImage.setPatterns(patternService.mapToPatterns(stateImageEntity.getPatterns()));
        stateImage.setOwnerStateName(stateImageEntity.getOwnerStateName());
        stateImage.setTimesActedOn(stateImageEntity.getTimesActedOn());
        stateImage.setShared(stateImageEntity.isShared());
        stateImage.setIndex(stateImageEntity.getIndex());
        stateImage.setDynamic(stateImageEntity.isDynamic());
        stateImage.setInvolvedTransitionIds(stateImageEntity.getInvolvedTransitionIds());
        return stateImage;
    }

    public Set<StateImageEntity> mapToStateImageEntitySet(Set<StateImage> stateImages, PatternService patternService) {
        return new HashSet<>(mapToStateImageEntityList(stateImages.stream().toList(), patternService));
    }

    public List<StateImageEntity> mapToStateImageEntityList(List<StateImage> stateImages, PatternService patternService) {
        List<StateImageEntity> stateImageEntityList = new ArrayList<>();
        stateImages.forEach(stateImage -> stateImageEntityList.add(map(stateImage, patternService)));
        return stateImageEntityList;
    }

    public Set<StateImage> mapToStateImageSet(Set<StateImageEntity> stateImageEntities, PatternService patternService) {
        return new HashSet<>(mapToStateImageList(stateImageEntities.stream().toList(), patternService));
    }

    public List<StateImage> mapToStateImageList(List<StateImageEntity> stateImageEntities, PatternService patternService) {
        List<StateImage> stateImageList = new ArrayList<>();
        stateImageEntities.forEach(stateImageEntity -> stateImageList.add(map(stateImageEntity, patternService)));
        return stateImageList;
    }
}
