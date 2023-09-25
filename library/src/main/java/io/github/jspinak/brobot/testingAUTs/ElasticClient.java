package io.github.jspinak.brobot.testingAUTs;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Getter
@Slf4j
public class ElasticClient {
    private Time time;

    private RestClient restClient;
    private ElasticsearchClient client;
    private List<ActionLogInfo> logBuffer = new ArrayList<>(); // Create a buffer to collect ActionLogInfo instances

    public ElasticClient(Time time) {
        this.time = time;
    }

    public void init() {
        restClient = RestClient
                .builder(HttpHost.create("http://localhost:9200"))
                .build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        client = new ElasticsearchClient(transport);
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
/*
        try {
            IndexResponse response = client.index(i -> i
                    .index("action")
                    .id(String.valueOf(actionId))
                    .document(actionLogInfo));
            log.info("Indexed with version: {}", response.version());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

 */
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

            BulkResponse result = client.bulk(br.build());

            // Log errors, if any
            if (result.errors()) {
                log.error("Bulk had errors");
                for (BulkResponseItem item: result.items()) {
                    if (item.error() != null) {
                        log.error(item.error().reason());
                    }
                }
            }

            restClient.close(); // Close the client when done

            /*

            // Create a list to hold your documents to index
            List<String> documentsToIndex = new ArrayList<>();
            for (ActionLogInfo logInfo : logBuffer) {
                documentsToIndex.add(logInfo.toJson());
            }
            // Bulk indexing
            StringBuilder bulkRequestBody = new StringBuilder();
            for (String document : documentsToIndex) {
                bulkRequestBody.append(document).append("\n");
            }
            Request request = new Request("POST", "/_bulk");
            request.setJsonEntity(bulkRequestBody.toString());
            Response response = restClient.performRequest(request);
            System.out.println(response.getStatusLine().getStatusCode()); // Handle the response as needed
            restClient.close(); // Close the client when done

             */
        }
    }

}
