package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.database.databaseMappers.StateEntityMapper;
import io.github.jspinak.brobot.app.database.repositories.StateRepo;
import io.github.jspinak.brobot.app.models.BuildModel;
import io.github.jspinak.brobot.app.web.requests.ProjectRequest;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.service.LogEntryService;
import io.github.jspinak.brobot.logging.AutomationSession;
import io.github.jspinak.brobot.testingAUTs.StateTraversalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AutomationService {
    private static final Logger logger = LoggerFactory.getLogger(AutomationService.class);

    private final BuildModel buildModel;
    private final Action action;
    private final StateRepo stateRepo;
    private final StateEntityMapper stateEntityMapper;
    private final StateTraversalService stateTraversalService;
    private final LogEntryService logEntryService;
    private final LogSenderService logSenderService;
    private final AutomationSession automationSession;

    public AutomationService(BuildModel buildModel, Action action, StateRepo stateRepo,
                             StateEntityMapper stateEntityMapper, StateTraversalService stateTraversalService,
                             LogEntryService logEntryService, LogSenderService logSenderService,
                             AutomationSession automationSession) {
        this.buildModel = buildModel;
        this.action = action;
        this.stateRepo = stateRepo;
        this.stateEntityMapper = stateEntityMapper;
        this.stateTraversalService = stateTraversalService;
        this.logEntryService = logEntryService;
        this.logSenderService = logSenderService;
        this.automationSession = automationSession;
    }

    public String integrateModelIntoFramework(ProjectRequest projectRequest) {
        buildModel.build(projectRequest.getId());
        return "Model built and integrating into the Brobot framework.";
    }

    public String testMouseMove() {
        action.perform(ActionOptions.Action.MOVE, new Location(500, 500).asObjectCollection());
        return "Moved mouse to 500, 500.";
    }

    public String testVisitAllImages() {
        stateRepo.findAll().forEach(stateEntity -> {
                State state = stateEntityMapper.map(stateEntity);
                state.getStateImages().forEach(sI -> action.perform(ActionOptions.Action.MOVE, sI.asObjectCollection()));
        });
        return "Moved mouse to all StateImages.";
    }

    public String visitAllStates() {
        String sessionId = automationSession.startNewSession();
        Set<Long> visitedStates = stateTraversalService.traverseAllStates();
        processAndSendLogs(sessionId);

        StringBuilder response = new StringBuilder();
        response.append("Visited states: ");
        visitedStates.forEach(stateId -> {
            stateRepo.findById(stateId).ifPresent(state ->
                    response.append(state.getName()).append(", "));
        });
        response.append("\nSession ID: ").append(sessionId);
        return response.toString();
    }

    private void processAndSendLogs(String sessionId) {
        try {
            List<LogEntry> logs = logEntryService.getLogEntriesBySessionId(sessionId);
            if (!logs.isEmpty()) {
                logger.info("Sending {} logs for session {}", logs.size(), sessionId);
                logSenderService.sendLogEntries(logs);
            } else {
                logger.info("No logs found for session {}", sessionId);
            }
        } catch (Exception e) {
            logger.error("Error processing and sending logs for session {}", sessionId, e);
            throw new RuntimeException("Failed to process and send logs for session: " + sessionId, e);
        }
    }

}