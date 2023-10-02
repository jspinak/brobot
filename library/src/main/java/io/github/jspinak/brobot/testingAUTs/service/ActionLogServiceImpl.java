package io.github.jspinak.brobot.testingAUTs.service;

import io.github.jspinak.brobot.testingAUTs.model.ActionLog;
import io.github.jspinak.brobot.testingAUTs.repository.ActionLogRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActionLogServiceImpl implements ActionLogService {

    private final ActionLogRepo actionLogRepo;

    public ActionLogServiceImpl(ActionLogRepo actionLogRepo) {
        this.actionLogRepo = actionLogRepo;
    }

    @Override
    public ActionLog createActionLog(ActionLog actionLog) {
        return actionLogRepo.save(actionLog);
    }

    @Override
    public Iterable<ActionLog> getAllActionLogs() {
        return actionLogRepo.findAll();
    }

    @Override
    public ActionLog updateActionLog(String id, ActionLog actionLog) {
        Optional<ActionLog> optionalActionLog = actionLogRepo.findById(id);
        if (optionalActionLog.isEmpty()) return null;
        ActionLog oldLog = optionalActionLog.get();
        oldLog.setStartTime(actionLog.getStartTime());
        oldLog.setEndTime(actionLog.getEndTime());
        oldLog.setAction(actionLog.getAction());
        oldLog.setSuccess(actionLog.isSuccess());
        oldLog.setImages(actionLog.getImages());
        oldLog.setOwnerStates(actionLog.getOwnerStates());
        return actionLogRepo.save(oldLog);
    }

    @Override
    public boolean deleteActionLog(String id) {
        Optional<ActionLog> optionalActionLog = actionLogRepo.findById(id);
        if (optionalActionLog.isEmpty()) return false;
        actionLogRepo.delete(optionalActionLog.get());
        return true;
    }

    @Override
    public ActionLog getActionLogById(String id) {
        return actionLogRepo.findById(id).orElse(null);
    }
}
