package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class StateImageEntityMapper {

    private final PatternEntityMapper patternEntityMapper;

    public StateImageEntityMapper(PatternEntityMapper patternEntityMapper) {
        this.patternEntityMapper = patternEntityMapper;
    }
    
    public StateImageEntity map(StateImage stateImage) {
        StateImageEntity stateImageEntity = new StateImageEntity();
        stateImageEntity.setProjectId(stateImage.getProjectId());
        stateImageEntity.setObjectType(stateImage.getObjectType());
        stateImageEntity.setName(stateImage.getName());
        stateImageEntity.setPatterns(patternEntityMapper.mapToPatternEntityList(stateImage.getPatterns()));
        stateImageEntity.setOwnerStateName(stateImage.getOwnerStateName());
        stateImageEntity.setTimesActedOn(stateImage.getTimesActedOn());
        stateImageEntity.setShared(stateImageEntity.isShared());
        stateImageEntity.setIndex(stateImage.getIndex());
        stateImageEntity.setDynamic(stateImage.isDynamic());
        stateImageEntity.setInvolvedTransitionIds(stateImage.getInvolvedTransitionIds());
        return stateImageEntity;
    }

    public StateImage map(StateImageEntity stateImageEntity) {
        StateImage stateImage = new StateImage();
        stateImage.setId(stateImageEntity.getId());
        stateImage.setProjectId(stateImageEntity.getProjectId());
        stateImage.setObjectType(stateImageEntity.getObjectType());
        stateImage.setName(stateImageEntity.getName());
        stateImage.setPatterns(patternEntityMapper.mapToPatternList(stateImageEntity.getPatterns()));
        stateImage.setOwnerStateName(stateImageEntity.getOwnerStateName());
        stateImage.setTimesActedOn(stateImageEntity.getTimesActedOn());
        stateImage.setShared(stateImageEntity.isShared());
        stateImage.setIndex(stateImageEntity.getIndex());
        stateImage.setDynamic(stateImageEntity.isDynamic());
        stateImage.setInvolvedTransitionIds(stateImageEntity.getInvolvedTransitionIds());
        return stateImage;
    }

    public Set<StateImageEntity> mapToStateImageEntitySet(Set<StateImage> stateImages) {
        return new HashSet<>(mapToStateImageEntityList(stateImages.stream().toList()));
    }

    public List<StateImageEntity> mapToStateImageEntityList(List<StateImage> stateImages) {
        List<StateImageEntity> stateImageEntityList = new ArrayList<>();
        stateImages.forEach(stateImage -> stateImageEntityList.add(map(stateImage)));
        return stateImageEntityList;
    }

    public Set<StateImage> mapToStateImageSet(Set<StateImageEntity> stateImageEntities) {
        return new HashSet<>(mapToStateImageList(stateImageEntities.stream().toList()));
    }

    public List<StateImage> mapToStateImageList(List<StateImageEntity> stateImageEntities) {
        List<StateImage> stateImageList = new ArrayList<>();
        stateImageEntities.forEach(stateImageEntity -> stateImageList.add(map(stateImageEntity)));
        return stateImageList;
    }
}
