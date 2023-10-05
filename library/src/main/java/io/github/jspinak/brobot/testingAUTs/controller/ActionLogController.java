package io.github.jspinak.brobot.testingAUTs.controller;

import io.github.jspinak.brobot.testingAUTs.service.ActionLogServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/actionlogs")
public class ActionLogController {

    private final ActionLogServiceImpl actionLogService;

    public ActionLogController(ActionLogServiceImpl actionLogService) {
        this.actionLogService = actionLogService;
    }

}
