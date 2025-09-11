package io.github.jspinak.brobot.persistence.provider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.database.entity.ActionRecordEntity;
import io.github.jspinak.brobot.persistence.database.entity.MatchEntity;
import io.github.jspinak.brobot.persistence.database.entity.RecordingSessionEntity;
import io.github.jspinak.brobot.persistence.database.repository.ActionRecordRepository;
import io.github.jspinak.brobot.persistence.database.repository.RecordingSessionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Database persistence provider using JPA. Extracted from the runner module to make it reusable.
 */
@Slf4j
public class DatabasePersistenceProvider extends AbstractPersistenceProvider {

    private final RecordingSessionRepository sessionRepository;
    private final ActionRecordRepository recordRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, RecordingSessionEntity> sessionCache = new ConcurrentHashMap<>();

    public DatabasePersistenceProvider(
            PersistenceConfiguration configuration,
            RecordingSessionRepository sessionRepository,
            ActionRecordRepository recordRepository) {
        super(configuration);
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.objectMapper = createObjectMapper();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        return mapper;
    }

    @Override
    @Transactional
    protected void doStartSession(String sessionId, SessionMetadata metadata) {
        RecordingSessionEntity entity = new RecordingSessionEntity();
        entity.setSessionId(sessionId);
        entity.setName(metadata.getName());
        entity.setApplication(metadata.getApplication());
        entity.setDescription(metadata.getMetadata());
        entity.setStartTime(metadata.getStartTime());
        entity.setStatus(RecordingSessionEntity.SessionStatus.ACTIVE);
        entity.setImported(false);
        entity.setTotalActions(0);
        entity.setSuccessfulActions(0);
        entity.setFailedActions(0);

        entity = sessionRepository.save(entity);
        sessionCache.put(sessionId, entity);

        log.debug("Created database session: {} (ID: {})", metadata.getName(), entity.getId());
    }

    @Override
    @Transactional
    protected void doStopSession(String sessionId, SessionMetadata metadata) {
        RecordingSessionEntity entity = sessionCache.get(sessionId);

        if (entity == null) {
            entity = sessionRepository.findBySessionId(sessionId).orElse(null);
        }

        if (entity != null) {
            entity.setEndTime(LocalDateTime.now());
            entity.setStatus(RecordingSessionEntity.SessionStatus.COMPLETED);
            entity.setDuration(Duration.between(entity.getStartTime(), entity.getEndTime()));

            sessionRepository.save(entity);
            sessionCache.remove(sessionId);

            log.debug(
                    "Finalized database session: {} (total actions: {})",
                    sessionId,
                    entity.getTotalActions());
        }
    }

    @Override
    @Transactional
    protected void doRecordAction(String sessionId, ActionRecord record, StateObject stateObject) {
        RecordingSessionEntity session = sessionCache.get(sessionId);

        if (session == null) {
            session = sessionRepository.findBySessionId(sessionId).orElse(null);
            if (session != null) {
                sessionCache.put(sessionId, session);
            }
        }

        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            return;
        }

        // Create action record entity
        ActionRecordEntity entity = new ActionRecordEntity();
        entity.setSession(session);
        entity.setTimestamp(LocalDateTime.now());
        entity.setActionSuccess(record.isActionSuccess());
        entity.setDuration((long) record.getDuration());
        entity.setText(record.getText());

        // Serialize action config
        if (record.getActionConfig() != null) {
            entity.setActionConfigType(record.getActionConfig().getClass().getSimpleName());
            try {
                entity.setActionConfigJson(
                        objectMapper.writeValueAsString(record.getActionConfig()));
            } catch (Exception e) {
                log.error("Failed to serialize action config", e);
            }
        }

        // Add state context if available
        if (stateObject != null) {
            entity.setStateName(stateObject.getOwnerStateName());
            entity.setObjectName(stateObject.getName());
        }

        // Convert matches
        if (record.getMatchList() != null && !record.getMatchList().isEmpty()) {
            Set<MatchEntity> matches =
                    record.getMatchList().stream()
                            .map(
                                    match -> {
                                        MatchEntity matchEntity = new MatchEntity();
                                        matchEntity.setActionRecord(entity);
                                        matchEntity.setX(match.x());
                                        matchEntity.setY(match.y());
                                        matchEntity.setWidth(match.w());
                                        matchEntity.setHeight(match.h());
                                        matchEntity.setSimScore(match.getScore());
                                        return matchEntity;
                                    })
                            .collect(Collectors.toSet());
            entity.setMatches(matches);
        }

        // Save record
        recordRepository.save(entity);

        // Update session stats
        session.setTotalActions(session.getTotalActions() + 1);
        if (record.isActionSuccess()) {
            session.setSuccessfulActions(session.getSuccessfulActions() + 1);
        } else {
            session.setFailedActions(session.getFailedActions() + 1);
        }
        session.setSuccessRate(
                (double) session.getSuccessfulActions() / session.getTotalActions() * 100);

        sessionRepository.save(session);

        log.trace("Recorded action to database for session: {}", sessionId);
    }

    @Override
    protected SessionMetadata doGetSessionMetadata(String sessionId) {
        RecordingSessionEntity entity = sessionCache.get(sessionId);

        if (entity == null) {
            entity = sessionRepository.findBySessionId(sessionId).orElse(null);
        }

        if (entity != null) {
            SessionMetadata metadata =
                    new SessionMetadata(
                            entity.getSessionId(), entity.getName(), entity.getApplication());
            metadata.setStartTime(entity.getStartTime());
            metadata.setEndTime(entity.getEndTime());
            metadata.setTotalActions(entity.getTotalActions());
            metadata.setSuccessfulActions(entity.getSuccessfulActions());
            metadata.setMetadata(entity.getDescription());

            return metadata;
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ActionHistory exportSession(String sessionId) {
        RecordingSessionEntity session = sessionRepository.findBySessionId(sessionId).orElse(null);

        if (session == null) {
            log.warn("Session not found for export: {}", sessionId);
            return null;
        }

        List<ActionRecordEntity> records =
                recordRepository.findBySessionIdOrderByTimestamp(session.getId());

        ActionHistory history = new ActionHistory();

        for (ActionRecordEntity entity : records) {
            try {
                ActionRecord record = convertEntityToRecord(entity);
                history.addSnapshot(record);
            } catch (Exception e) {
                log.error("Failed to convert record entity", e);
            }
        }

        log.info("Exported database session {} with {} records", sessionId, records.size());
        return history;
    }

    @Override
    @Transactional
    public String importSession(ActionHistory history, String sessionName) {
        String sessionId = UUID.randomUUID().toString();

        // Create session
        RecordingSessionEntity session = new RecordingSessionEntity();
        session.setSessionId(sessionId);
        session.setName(sessionName);
        session.setApplication("Imported");
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now());
        session.setStatus(RecordingSessionEntity.SessionStatus.COMPLETED);
        session.setImported(true);
        session.setTotalActions(history.getSnapshots().size());
        session.setSuccessfulActions(
                (int)
                        history.getSnapshots().stream()
                                .filter(ActionRecord::isActionSuccess)
                                .count());
        session.setFailedActions(session.getTotalActions() - session.getSuccessfulActions());
        session.setSuccessRate(
                session.getTotalActions() > 0
                        ? (double) session.getSuccessfulActions() / session.getTotalActions() * 100
                        : 0);

        session = sessionRepository.save(session);

        // Import records
        for (ActionRecord record : history.getSnapshots()) {
            ActionRecordEntity entity = new ActionRecordEntity();
            entity.setSession(session);
            entity.setTimestamp(LocalDateTime.now());
            entity.setActionSuccess(record.isActionSuccess());
            entity.setDuration((long) record.getDuration());
            entity.setText(record.getText());

            if (record.getActionConfig() != null) {
                entity.setActionConfigType(record.getActionConfig().getClass().getSimpleName());
                try {
                    entity.setActionConfigJson(
                            objectMapper.writeValueAsString(record.getActionConfig()));
                } catch (Exception e) {
                    log.error("Failed to serialize action config during import", e);
                }
            }

            recordRepository.save(entity);
        }

        log.info(
                "Imported session {} with {} records to database",
                sessionName,
                history.getSnapshots().size());
        return sessionId;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(RecordingSessionEntity::getSessionId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        RecordingSessionEntity session = sessionRepository.findBySessionId(sessionId).orElse(null);

        if (session != null) {
            // Delete all records (cascade should handle this)
            recordRepository.deleteBySessionId(session.getId());

            // Delete session
            sessionRepository.delete(session);
            sessionCache.remove(sessionId);

            log.info("Deleted database session: {}", sessionId);
        }
    }

    private ActionRecord convertEntityToRecord(ActionRecordEntity entity) {
        ActionRecord.Builder builder = new ActionRecord.Builder();

        builder.setActionSuccess(entity.isActionSuccess());
        builder.setDuration(entity.getDuration());
        builder.setText(entity.getText());

        // Deserialize action config if needed
        // This would require the actual config class, so we'll skip for now

        // Convert matches
        if (entity.getMatches() != null) {
            for (MatchEntity matchEntity : entity.getMatches()) {
                Match match =
                        new Match.Builder()
                                .setRegion(
                                        matchEntity.getX(),
                                        matchEntity.getY(),
                                        matchEntity.getWidth(),
                                        matchEntity.getHeight())
                                .setSimScore(matchEntity.getSimScore())
                                .build();
                builder.addMatch(match);
            }
        }

        return builder.build();
    }
}
