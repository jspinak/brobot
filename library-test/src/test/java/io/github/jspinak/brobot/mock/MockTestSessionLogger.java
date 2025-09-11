package io.github.jspinak.brobot.mock;

import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.logging.SessionLifecycleLogger;

@Component
@Primary
public class MockTestSessionLogger implements SessionLifecycleLogger {

    @Override
    public String startSession(String applicationUnderTest) {
        String sessionId = UUID.randomUUID().toString();
        System.out.println(
                "Mock startSession: ApplicationUnderTest="
                        + applicationUnderTest
                        + ", SessionId="
                        + sessionId);
        return sessionId;
    }

    @Override
    public void endSession(String sessionId) {
        System.out.println("Mock endSession: SessionId=" + sessionId);
    }

    @Override
    public void setCurrentState(String sessionId, String stateName, String stateDescription) {
        System.out.println(
                "Mock setCurrentState: SessionId="
                        + sessionId
                        + ", StateName="
                        + stateName
                        + ", StateDescription="
                        + stateDescription);
    }
}
