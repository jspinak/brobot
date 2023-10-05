package io.github.jspinak.brobot.testingAUTs.zFridge;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;

@Component
@Getter
public class RestClientConnection {
    private final ElasticsearchClient elasticsearchClient;
    private final RestClient restClient;

    public RestClientConnection() {
        // Disable SSL certificate validation (for testing purposes only)
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "https"))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    try {
                        SSLContext sslContext = SSLContexts.custom()
                                .loadTrustMaterial(new TrustSelfSignedStrategy())
                                .build();
                        httpClientBuilder.setSSLContext(sslContext);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to configure SSL context", e);
                    }
                    return httpClientBuilder;
                });

        restClient = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        elasticsearchClient = new ElasticsearchClient(transport);


        //restClient = RestClient.builder(new HttpHost("localhost", 9200, "https")).build();
        //ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        //elasticsearchClient = new ElasticsearchClient(transport);
    }

    public void close() {
        try {
            restClient.close();
        } catch (Exception e) {
            // Handle exception if necessary
        }
    }
}
