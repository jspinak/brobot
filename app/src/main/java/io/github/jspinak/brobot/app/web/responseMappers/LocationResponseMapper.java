package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.LocationEntity;
import io.github.jspinak.brobot.app.web.requests.LocationRequest;
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

    public LocationEntity fromRequest(LocationRequest request) {
        if (request == null) {
            return null;
        }
        LocationEntity entity = new LocationEntity();
        entity.setId(request.getId());
        entity.setName(request.getName());
        entity.setDefinedByXY(request.isDefinedByXY());
        entity.setLocX(request.getLocX());
        entity.setLocY(request.getLocY());
        entity.setRegion(regionResponseMapper.fromRequest(request.getRegion()));
        entity.setPosition(positionResponseMapper.fromRequest(request.getPosition()));

        // Convert String to Positions.Name enum
        if (request.getAnchor() != null) {
            try {
                entity.setAnchor(Positions.Name.valueOf(request.getAnchor()));
            } catch (IllegalArgumentException e) {
                // Handle the case where the string doesn't match any enum constant
                // You might want to log this error or throw a custom exception
                throw new IllegalArgumentException("Invalid anchor value: " + request.getAnchor(), e);
            }
        }

        return entity;
    }

    public LocationRequest toRequest(LocationEntity entity) {
        if (entity == null) {
            return null;
        }

        LocationRequest request = new LocationRequest();
        request.setId(entity.getId());
        request.setName(entity.getName());
        request.setDefinedByXY(entity.isDefinedByXY());
        request.setLocX(entity.getLocX());
        request.setLocY(entity.getLocY());
        request.setRegion(regionResponseMapper.toRequest(entity.getRegion()));
        request.setPosition(positionResponseMapper.toRequest(entity.getPosition()));
        request.setAnchor(entity.getAnchor() != null ? entity.getAnchor().name() : null);

        return request;
    }
}
