package io.github.jspinak.brobot.testingAUTs;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Getter
@Slf4j
public class ActionLogSender {
    private final RestClientConnection restClientConnection;
    private final Time time;

    private List<ActionLogInfo> logBuffer = new ArrayList<>(); // Create a buffer to collect ActionLogInfo instances

    public ActionLogSender(RestClientConnection restClientConnection, Time time) {
        this.restClientConnection = restClientConnection;
        this.time = time;
    }

    public void indexAction(int actionId, Matches matches, ActionOptions actionOptions,
                            ObjectCollection... objectCollections) {
        ActionLogInfo actionLogInfo = new ActionLogInfo(actionId,
                time.getStartTime(actionOptions.getAction()),
                time.getEndTime(actionOptions.getAction()),
                matches, actionOptions, objectCollections);
        // Log the action info to the buffer
        logBuffer.add(actionLogInfo);
        // Log the instance to a file or console
        log.info("Logged ActionLogInfo: {}", actionLogInfo.toJson());
    }

    // Method to send logs to Elasticsearch in batches
    public void sendLogsToElasticsearch() throws IOException {
        Report.println("send logs");
        if (!logBuffer.isEmpty()) {
            BulkRequest.Builder br = new BulkRequest.Builder();
            for (ActionLogInfo logInfo : logBuffer) {
                String jsonLogInfo = logInfo.toJson();
                System.out.println(jsonLogInfo); // Print the JSON content
                br.operations(op -> op
                        .index(idx -> idx
                            .index("actions")
                            .id(String.valueOf(logInfo.getActionId()))
                            .document(logInfo.toJson()))); // toJson is necessary because of the LocalDateTime field
            }

            BulkResponse result = restClientConnection.getElasticsearchClient().bulk(br.build());

            // Log errors, if any
            if (result.errors()) {
                log.error("Bulk had errors");
                for (BulkResponseItem item: result.items()) {
                    if (item.error() != null) {
                        log.error(item.error().reason());
                    }
                }
            }
        }
    }

}
