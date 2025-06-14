// Create this new file in: runner/src/main/.../persistence/mapper/LogMapper.java
package io.github.jspinak.brobot.runner.persistence.mapper;

import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.model.PerformanceMetricsData;
import io.github.jspinak.brobot.runner.persistence.entity.LogEntry;
import org.springframework.stereotype.Component;

@Component
public class LogMapper {

    public LogData toLogData(LogEntry entity) {
        if (entity == null) {
            return null;
        }

        LogData logData = new LogData();
        logData.setSessionId(entity.getSessionId());
        logData.setDescription(entity.getDescription());
        logData.setSuccess(entity.isSuccess());
        logData.setTimestamp(entity.getTimestamp());
        logData.setType(entity.getType());

        // Map performance metrics
        if (entity.getPerformance() != null) {
            PerformanceMetricsData metricsData = new PerformanceMetricsData();
            metricsData.setActionDuration(entity.getPerformance().getActionDuration());
            //... map other performance fields
            logData.setPerformance(metricsData);
        }

        // ... map other fields from entity to logData as needed by the UI

        return logData;
    }
}
