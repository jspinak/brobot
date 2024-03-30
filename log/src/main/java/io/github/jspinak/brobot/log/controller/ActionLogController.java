package io.github.jspinak.brobot.log.controller;

import io.github.jspinak.brobot.log.model.ActionLogDTO;
import io.github.jspinak.brobot.log.service.ActionLogServiceImpl;
import io.github.jspinak.brobot.testingAUTs.ActionLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/actionlogs")
public class ActionLogController {

    private final ActionLogServiceImpl actionLogService;

    public ActionLogController(ActionLogServiceImpl actionLogService) {
        this.actionLogService = actionLogService;
    }

    @PostMapping
    public ResponseEntity<ActionLog> createActionLog(@RequestBody ActionLog actionLog) {
        ActionLogDTO createdActionLog = actionLogService.createActionLog(actionLog);
        if (createdActionLog != null) {
            return new ResponseEntity<>(actionLog, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}


