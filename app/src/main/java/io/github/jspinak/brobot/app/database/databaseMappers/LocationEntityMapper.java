package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.LocationEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;

public class LocationEntityMapper {

    // not all locations have a region, position, or anchor
    public static LocationEntity map(Location location) {
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setName(location.getName());
        locationEntity.setLocX(location.getX());
        if (location.getRegion() != null) locationEntity.setRegion(RegionEmbeddableMapper.map(location.getRegion()));
        if (location.getPosition() != null) locationEntity.setPosition(PositionEmbeddableMapper.map(location.getPosition()));
        if (location.getAnchor() != null) locationEntity.setAnchor(location.getAnchor());
        return locationEntity;
    }

    public static Location map(LocationEntity locationEntity) {
        Location location = new Location();
        location.setName(locationEntity.getName());
        location.setX(locationEntity.getLocX());
        location.setY(locationEntity.getLocY());
        if (locationEntity.getRegion() != null) location.setRegion(RegionEmbeddableMapper.map(locationEntity.getRegion()));
        if (locationEntity.getPosition() != null) location.setPosition(PositionEmbeddableMapper.map(locationEntity.getPosition()));
        if (locationEntity.getAnchor() != null) location.setAnchor(locationEntity.getAnchor());
        return location;
    }
}
