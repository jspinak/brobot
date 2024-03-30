package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.MatchHistoryEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = AnchorMapper.class)
@Component
public interface MatchHistoryMapper {
    MatchHistoryMapper INSTANCE = Mappers.getMapper(MatchHistoryMapper.class);

    @Mapping(source = "snapshots", target = "snapshots", qualifiedByName = "mapToMatchSnapshotEntity")
    MatchHistoryEntity mapToEntity(MatchHistory matchHistory);

    @Mapping(source = "snapshots", target = "snapshots", qualifiedByName = "mapFromMatchSnapshotEntity")
    MatchHistory mapFromEntity(MatchHistoryEntity matchHistoryEntity);
}