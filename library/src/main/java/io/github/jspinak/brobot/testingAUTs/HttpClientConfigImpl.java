package io.github.jspinak.brobot.testingAUTs;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.File;

@Configuration
public class HttpClientConfigImpl implements RestClientBuilder.HttpClientConfigCallback {
    @Value("${elasticsearch.truststore.location}") // Assuming this property exists in your application.properties or application.yml
    private String trustStoreLocation;

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        String elasticUsername = System.getProperty("elastic.username");
        String elasticPassword = System.getProperty("elastic.password");
        try {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(elasticUsername, elasticPassword);
            credentialsProvider.setCredentials(AuthScope.ANY, usernamePasswordCredentials);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            File trustStoreLocationFile = new File("C:\\tmp\\truststore.p12");
            SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(trustStoreLocationFile, "password".toCharArray());
            SSLContext sslContext = sslContextBuilder.build();
            httpClientBuilder.setSSLContext(sslContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpClientBuilder;
    }
}
