package io.github.jspinak.brobot.log;

import io.github.jspinak.brobot.log.service.ActionLogService;
import io.github.jspinak.brobot.testingAUTs.ActionLog;
import io.github.jspinak.brobot.testingAUTs.LogEventListener;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchLogWriter implements LogEventListener {

    private final ActionLogService actionLogService;

    public ElasticsearchLogWriter(ActionLogService actionLogService) {
        this.actionLogService = actionLogService;
    }

    @Override
    public void handleLogEvent(ActionLog actionLog) {
        actionLogService.createActionLog(actionLog);
    }
}
