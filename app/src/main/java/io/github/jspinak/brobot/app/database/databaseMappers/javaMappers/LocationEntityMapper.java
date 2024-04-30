package io.github.jspinak.brobot.app.database.databaseMappers.javaMappers;

import io.github.jspinak.brobot.app.database.embeddable.PositionEmbeddable;
import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.app.database.entities.LocationEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;

public class LocationEntityMapper {

    public static LocationEntity map(Location location) {
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setName(location.getName());
        locationEntity.setLocX(location.getX());
        locationEntity.setRegion(RegionEmbeddableMapper.map(location.getRegion()));
        locationEntity.setPosition(PositionEmbeddableMapper.map(location.getPosition()));
        locationEntity.setAnchor(location.getAnchor());
        return locationEntity;
    }

    public static Location map(LocationEntity locationEntity) {
        Location location = new Location();
        location.setName(locationEntity.getName());
        location.setX(locationEntity.getLocX());
        location.setY(locationEntity.getLocY());
        location.setRegion(RegionEmbeddableMapper.map(locationEntity.getRegion()));
        location.setPosition(PositionEmbeddableMapper.map(locationEntity.getPosition()));
        location.setAnchor(locationEntity.getAnchor());
        return location;
    }
}
