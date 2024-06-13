package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StateImageEntityMapper {
    
    public static StateImageEntity map(StateImage stateImage) {
        StateImageEntity stateImageEntity = new StateImageEntity();
        stateImageEntity.setProjectId(stateImage.getProjectId());
        stateImageEntity.setObjectType(stateImage.getObjectType());
        stateImageEntity.setName(stateImage.getName());
        stateImageEntity.setPatterns(PatternEntityMapper.mapToPatternEntityList(stateImage.getPatterns()));
        stateImageEntity.setOwnerStateName(stateImage.getOwnerStateName());
        stateImageEntity.setTimesActedOn(stateImage.getTimesActedOn());
        stateImageEntity.setShared(stateImageEntity.isShared());
        stateImageEntity.setIndex(stateImage.getIndex());
        stateImageEntity.setDynamic(stateImage.isDynamic());
        return stateImageEntity;
    }

    public static StateImage map(StateImageEntity stateImageEntity) {
        StateImage stateImage = new StateImage();
        stateImage.setProjectId(stateImageEntity.getProjectId());
        stateImage.setObjectType(stateImageEntity.getObjectType());
        stateImage.setName(stateImageEntity.getName());
        stateImage.setPatterns(PatternEntityMapper.mapToPatternList(stateImageEntity.getPatterns()));
        stateImage.setOwnerStateName(stateImageEntity.getOwnerStateName());
        stateImage.setTimesActedOn(stateImageEntity.getTimesActedOn());
        stateImage.setShared(stateImageEntity.isShared());
        stateImage.setIndex(stateImageEntity.getIndex());
        stateImage.setDynamic(stateImageEntity.isDynamic());
        return stateImage;
    }

    public static Set<StateImageEntity> mapToStateImageEntitySet(Set<StateImage> stateImages) {
        return new HashSet<>(mapToStateImageEntityList(stateImages.stream().toList()));
    }

    public static List<StateImageEntity> mapToStateImageEntityList(List<StateImage> stateImages) {
        List<StateImageEntity> stateImageEntityList = new ArrayList<>();
        stateImages.forEach(stateImage -> stateImageEntityList.add(map(stateImage)));
        return stateImageEntityList;
    }

    public static Set<StateImage> mapToStateImageSet(Set<StateImageEntity> stateImageEntities) {
        return new HashSet<>(mapToStateImageList(stateImageEntities.stream().toList()));
    }

    public static List<StateImage> mapToStateImageList(List<StateImageEntity> stateImageEntities) {
        List<StateImage> stateImageList = new ArrayList<>();
        stateImageEntities.forEach(stateImageEntity -> stateImageList.add(map(stateImageEntity)));
        return stateImageList;
    }
}
