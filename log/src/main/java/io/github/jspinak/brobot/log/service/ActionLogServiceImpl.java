package io.github.jspinak.brobot.log.service;

import io.github.jspinak.brobot.log.model.ActionLogDTO;
import io.github.jspinak.brobot.log.model.ActionLogMapper;
import io.github.jspinak.brobot.log.repository.ActionLogRepo;
import io.github.jspinak.brobot.testingAUTs.ActionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ActionLogServiceImpl implements ActionLogService {

    @Autowired
    private ActionLogRepo actionLogRepo;

    @Autowired
    private ActionLogMapper actionLogMapper;

    @Override
    public ActionLogDTO createActionLog(ActionLog actionLog) {
        return actionLogRepo.save(actionLogMapper.mapToDTO(actionLog));
    }

    @Override
    public List<ActionLog> getAllActionLogs() {
        Iterable<ActionLogDTO> actionLogDTOs = actionLogRepo.findAll();
        List<ActionLog> actionLogList = new ArrayList<>();
        actionLogDTOs.forEach(actionLogDTO -> actionLogList.add(actionLogMapper.mapFromDTO(actionLogDTO)));
        return actionLogList;
    }

    @Override
    public ActionLog updateActionLog(String id, ActionLog actionLog) {
        Optional<ActionLogDTO> optionalActionLog = actionLogRepo.findById(id);
        if (optionalActionLog.isEmpty()) return null;
        ActionLog oldLog = actionLogMapper.mapFromDTO(optionalActionLog.get());
        //oldLog.setStartTime(actionLog.getStartTime());
        //oldLog.setEndTime(actionLog.getEndTime());
        oldLog.setAction(actionLog.getAction());
        //oldLog.setSuccess(actionLog.isSuccess());
        //oldLog.setImages(actionLog.getImages());
        //oldLog.setOwnerStates(actionLog.getOwnerStates());
        actionLogRepo.save(actionLogMapper.mapToDTO(oldLog));
        return oldLog;
    }

    @Override
    public boolean deleteActionLog(String id) {
        Optional<ActionLogDTO> optionalActionLog = actionLogRepo.findById(id);
        if (optionalActionLog.isEmpty()) return false;
        actionLogRepo.delete(optionalActionLog.get());
        return true;
    }

    @Override
    public Optional<ActionLog> getActionLogById(String id) {
        Optional<ActionLogDTO> actionLogDTO = actionLogRepo.findById(id);
        return actionLogDTO.map(logDTO -> actionLogMapper.mapFromDTO(logDTO));
    }
}


