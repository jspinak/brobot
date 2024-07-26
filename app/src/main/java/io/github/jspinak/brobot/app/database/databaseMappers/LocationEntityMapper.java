package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.LocationEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationEntityMapper {

    private final RegionEmbeddableMapper regionEmbeddableMapper;
    private final PositionEmbeddableMapper positionEmbeddableMapper;

    public LocationEntityMapper(RegionEmbeddableMapper regionEmbeddableMapper,
                                PositionEmbeddableMapper positionEmbeddableMapper) {
        this.regionEmbeddableMapper = regionEmbeddableMapper;
        this.positionEmbeddableMapper = positionEmbeddableMapper;
    }

    // not all locations have a region, position, or anchor
    public LocationEntity map(Location location) {
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setName(location.getName());
        locationEntity.setLocX(location.getX());
        if (location.getRegion() != null) locationEntity.setRegion(regionEmbeddableMapper.map(location.getRegion()));
        if (location.getPosition() != null) locationEntity.setPosition(positionEmbeddableMapper.map(location.getPosition()));
        if (location.getAnchor() != null) locationEntity.setAnchor(location.getAnchor());
        return locationEntity;
    }

    public Location map(LocationEntity locationEntity) {
        Location location = new Location();
        location.setName(locationEntity.getName());
        location.setX(locationEntity.getLocX());
        location.setY(locationEntity.getLocY());
        if (locationEntity.getRegion() != null) location.setRegion(regionEmbeddableMapper.map(locationEntity.getRegion()));
        if (locationEntity.getPosition() != null) location.setPosition(positionEmbeddableMapper.map(locationEntity.getPosition()));
        if (locationEntity.getAnchor() != null) location.setAnchor(locationEntity.getAnchor());
        return location;
    }
}
