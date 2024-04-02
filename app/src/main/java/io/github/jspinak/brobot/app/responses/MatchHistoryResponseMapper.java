package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {MatchSnapshotResponseMapper.class})
public interface MatchHistoryResponseMapper {
    MatchHistoryResponseMapper INSTANCE = Mappers.getMapper(MatchHistoryResponseMapper.class);

    @Mapping(source = "snapshots", target = "snapshots")
    MatchHistoryResponse map(MatchHistory matchHistory);

    @Mapping(source = "snapshots", target = "snapshots")
    MatchHistory map(MatchHistoryResponse matchHistoryResponse);
}