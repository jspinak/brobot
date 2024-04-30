package io.github.jspinak.brobot.app.database.databaseMappers.mapstructMappers;

import io.github.jspinak.brobot.app.database.entities.MatchHistoryEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = MatchSnapshotMapper.class)
public interface MatchHistoryMapper {
    MatchHistoryMapper INSTANCE = Mappers.getMapper(MatchHistoryMapper.class);

    @Mapping(source = "snapshots", target = "snapshots")
    @Mapping(target = "id", ignore = true)
    MatchHistoryEntity map(MatchHistory matchHistory);

    @Mapping(source = "snapshots", target = "snapshots")
    MatchHistory map(MatchHistoryEntity matchHistoryEntity);
}