package io.github.jspinak.brobot.testingAUTs.service;

import io.github.jspinak.brobot.testingAUTs.model.ActionLog;

import java.util.List;

public interface ActionLogService {

    ActionLog createActionLog(ActionLog actionLog);
    Iterable<ActionLog> getAllActionLogs();
    ActionLog updateActionLog(String id, ActionLog actionLog);
    boolean deleteActionLog(String id);
    ActionLog getActionLogById(String id);
}
