package io.github.jspinak.brobot.log.configuration;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.File;

@Configuration
public class HttpClientConfigImpl implements RestClientBuilder.HttpClientConfigCallback {
    //@Value("${elasticsearch.truststore.location}") // Assuming this property exists in your application.properties or application.yml
    //private String trustStoreLocation;

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        //String elasticUsername = System.getProperty("elastic.username");
        //String elasticPassword = System.getProperty("elastic.password");
        //String truststoreLocation = System.getProperty("spring.elasticsearch.rest.ssl.trust-store-location");
        //String truststorePassword = System.getProperty("spring.elasticsearch.rest.ssl.trust-store-password");
        try {
            System.out.println("configure elasticsearch");
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials("elastic", "q6MXQ2zTW7Abf_LS7f_W");
            credentialsProvider.setCredentials(AuthScope.ANY, usernamePasswordCredentials);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            String trustStoreLocation = "C:\\Users\\jspin\\Documents\\brobot_parent\\elasticsearch\\certs\\truststore.p12";
            File trustStoreLocationFile = new File(trustStoreLocation);
            SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(trustStoreLocationFile, "password".toCharArray());
            SSLContext sslContext = sslContextBuilder.build();
            httpClientBuilder.setSSLContext(sslContext);
        } catch (Exception e) {
            System.out.println("connection failed");
            e.printStackTrace();
        }
        return httpClientBuilder;
    }
}
