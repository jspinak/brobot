package io.github.jspinak.brobot.logging.impl;

import io.github.jspinak.brobot.logging.TestSessionLogger;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Qualifier("sessionLoggerImpl")
public class TestSessionLoggerImpl implements TestSessionLogger {
    private static final Logger logger = LoggerFactory.getLogger(TestSessionLoggerImpl.class);

    private final Map<String, SessionInfo> activeSessions = new HashMap<>();

    @Override
    public String startSession(String applicationUnderTest) {
        String sessionId = UUID.randomUUID().toString();
        SessionInfo sessionInfo = new SessionInfo(sessionId, applicationUnderTest);
        activeSessions.put(sessionId, sessionInfo);

        logger.info("Started session {} for application: {}", sessionId, applicationUnderTest);
        return sessionId;
    }

    @Override
    public void endSession(String sessionId) {
        if (activeSessions.containsKey(sessionId)) {
            SessionInfo session = activeSessions.get(sessionId);
            session.setEndTime(LocalDateTime.now());
            logger.info("Ended session {} after {} seconds", sessionId,
                    session.getDurationInSeconds());
            activeSessions.remove(sessionId);
        } else {
            logger.warn("Attempted to end unknown session: {}", sessionId);
        }
    }

    @Override
    public void setCurrentState(String sessionId, String stateName, String stateDescription) {
        if (activeSessions.containsKey(sessionId)) {
            SessionInfo session = activeSessions.get(sessionId);
            session.setCurrentState(stateName, stateDescription);
            logger.info("Session {} state changed to: {} - {}",
                    sessionId, stateName, stateDescription);
        } else {
            logger.warn("Attempted to update state for unknown session: {}", sessionId);
        }
    }

    private static class SessionInfo {
        private final String id;
        private final String application;
        private final LocalDateTime startTime;
        @Setter
        private LocalDateTime endTime;
        private String currentStateName;
        private String currentStateDescription;

        public SessionInfo(String id, String application) {
            this.id = id;
            this.application = application;
            this.startTime = LocalDateTime.now();
        }

        public void setCurrentState(String stateName, String stateDescription) {
            this.currentStateName = stateName;
            this.currentStateDescription = stateDescription;
        }

        public long getDurationInSeconds() {
            LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
            return java.time.Duration.between(startTime, end).getSeconds();
        }
    }
}