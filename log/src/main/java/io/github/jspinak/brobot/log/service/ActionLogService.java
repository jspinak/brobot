package io.github.jspinak.brobot.log.service;

import io.github.jspinak.brobot.log.model.ActionLogDTO;
import io.github.jspinak.brobot.testingAUTs.ActionLog;

import java.util.List;
import java.util.Optional;

public interface ActionLogService {

    ActionLogDTO createActionLog(ActionLog actionLog);
    List<ActionLog> getAllActionLogs();
    ActionLog updateActionLog(String id, ActionLog actionLog);
    boolean deleteActionLog(String id);
    Optional<ActionLog> getActionLogById(String id);
}
