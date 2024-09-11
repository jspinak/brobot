package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.database.databaseMappers.StateEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ProjectEntity;
import io.github.jspinak.brobot.app.database.repositories.StateRepo;
import io.github.jspinak.brobot.app.models.BuildModel;
import io.github.jspinak.brobot.app.web.requests.ProjectRequest;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.service.LogEntryService;
import io.github.jspinak.brobot.logging.AutomationSession;
import io.github.jspinak.brobot.testingAUTs.StateTraversalService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomationService {

    private final BuildModel buildModel;
    private final Action action;
    private final StateRepo stateRepo;
    private final StateEntityMapper stateEntityMapper;
    private final StateTraversalService stateTraversalService;
    private final WebSocketService webSocketService;
    private final LogEntryService logEntryService;
    private final AutomationSession automationSession;

    public AutomationService(BuildModel buildModel, Action action, StateRepo stateRepo,
                             StateEntityMapper stateEntityMapper, StateTraversalService stateTraversalService,
                             WebSocketService webSocketService, LogEntryService logEntryService,
                             AutomationSession automationSession) {
        this.buildModel = buildModel;
        this.action = action;
        this.stateRepo = stateRepo;
        this.stateEntityMapper = stateEntityMapper;
        this.stateTraversalService = stateTraversalService;
        this.webSocketService = webSocketService;
        this.logEntryService = logEntryService;
        this.automationSession = automationSession;
    }

    public String integrateModelIntoFramework(ProjectRequest projectRequest) {
        buildModel.build(projectRequest.getId(), projectRequest.getName());
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
        stateTraversalService.traverseAllStates();
        processAndSendLogs(sessionId);
        return "Visited all states. Session ID: " + sessionId;
    }

    // Retrieve and send all logs for this session
    public void processAndSendLogs(String sessionId) {
        List<LogEntry> logs = logEntryService.getLogEntriesBySessionId(sessionId);
        webSocketService.sendLogUpdate(logs);
    }


}