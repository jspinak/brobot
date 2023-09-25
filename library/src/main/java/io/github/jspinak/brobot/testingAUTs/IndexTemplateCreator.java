package io.github.jspinak.brobot.testingAUTs;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

@Component
public class IndexTemplateCreator {

    public void create() throws Exception {
        // Create a connection to your Elasticsearch cluster
        RestClient client = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();

        try {
            // Define the index template JSON
            String indexTemplateJson = """
                {
                  "index_patterns": ["action-*"],  // Match indices with names starting with "action-"
                  "settings": {
                    "number_of_shards": 1,
                    "number_of_replicas": 1
                  },
                  "mappings": {
                    "properties": {
                      "actionId": {
                        "type": "integer"
                      },
                      "startTime": {
                        "type": "date"
                      },
                      "endTime": {
                        "type": "date"
                      },
                      "action": {
                        "type": "keyword"  // Map the enum field as a keyword
                      },
                      "success": {
                        "type": "boolean"
                      },
                      "images": {
                        "type": "keyword"
                      },
                      "ownerStates": {
                        "type": "keyword"
                      }
                    }
                  }
                }
                """;

            // Create a PUT request to create the index template
            Request request = new Request("PUT", "/_template/action_template");
            request.setJsonEntity(indexTemplateJson);

            // Execute the request
            client.performRequest(request);

            System.out.println("Index template created successfully.");
        } finally {
            // Close the Elasticsearch client when done
            client.close();
        }
    }
}
