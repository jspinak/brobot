package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.LocationEntity;
import io.github.jspinak.brobot.app.web.responses.LocationResponse;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import org.springframework.stereotype.Component;

@Component
public class LocationResponseMapper {

    private final RegionResponseMapper regionResponseMapper;
    private final PositionResponseMapper positionResponseMapper;

    public LocationResponseMapper(RegionResponseMapper regionResponseMapper, PositionResponseMapper positionResponseMapper) {
        this.regionResponseMapper = regionResponseMapper;
        this.positionResponseMapper = positionResponseMapper;
    }

    public LocationResponse map(LocationEntity locationEntity) {
        if (locationEntity == null) {
            return null;
        }
        LocationResponse locationResponse = new LocationResponse();
        locationResponse.setId(locationEntity.getId());
        locationResponse.setName(locationEntity.getName());
        locationResponse.setDefinedByXY(locationEntity.isDefinedByXY());
        locationResponse.setLocX(locationEntity.getLocX());
        locationResponse.setLocY(locationEntity.getLocY());
        locationResponse.setRegion(regionResponseMapper.map(locationEntity.getRegion()));
        locationResponse.setPosition(positionResponseMapper.map(locationEntity.getPosition()));
        locationResponse.setAnchor(locationEntity.getAnchor().name());
        return locationResponse;
    }

    public LocationEntity map(LocationResponse locationResponse) {
        if (locationResponse == null) {
            return null;
        }
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setId(locationResponse.getId());
        locationEntity.setName(locationResponse.getName());
        locationEntity.setDefinedByXY(locationResponse.isDefinedByXY());
        locationEntity.setLocX(locationResponse.getLocX());
        locationEntity.setLocY(locationResponse.getLocY());
        locationEntity.setRegion(regionResponseMapper.map(locationResponse.getRegion()));
        locationEntity.setPosition(positionResponseMapper.map(locationResponse.getPosition()));
        locationEntity.setAnchor(Positions.Name.valueOf(locationResponse.getAnchor()));
        return locationEntity;
    }
}
