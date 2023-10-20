package io.github.jspinak.brobot.testingAUTs.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;

// currently not using this configuration
/*@Configuration
public class ElasticsearchClientConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.rest.uris}")
    String connectionUrl;

    @Value("${elastic.username}")
    String username;

    @Value("${elastic.password}")
    String password;

    @Value("${spring.elasticsearch.client.certificate}")
    String certificateBase64;

    @Override
    public ClientConfiguration clientConfiguration() {
        try{
            return ClientConfiguration.builder()
                    .connectedTo("localhost:9200")
                    .usingSsl(getSSLContext())
                    .withBasicAuth("elastic", "brobot-testing-repo")
                    .build();
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private SSLContext getSSLContext() throws
            CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        byte[] decode = Base64.getDecoder().decode("LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlEU2pDQ0FqS2dBd0lCQWdJVkFNd1NFOHpPODJ3RGZKalJQRFJFdUNUdWxkSThNQTBHQ1NxR1NJYjNEUUVCDQpDd1VBTURReE1qQXdCZ05WQkFNVEtVVnNZWE4wYVdNZ1EyVnlkR2xtYVdOaGRHVWdWRzl2YkNCQmRYUnZaMlZ1DQpaWEpoZEdWa0lFTkJNQjRYRFRJek1UQXdNakV4TURZeU9Wb1hEVEkyTVRBd01URXhNRFl5T1Zvd05ERXlNREFHDQpBMVVFQXhNcFJXeGhjM1JwWXlCRFpYSjBhV1pwWTJGMFpTQlViMjlzSUVGMWRHOW5aVzVsY21GMFpXUWdRMEV3DQpnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFEV2k1KzNxbWdEWURBVWhRYk0yUm9ZDQpNZ1Y5b1JHck5udkNlbzJET0VycnpOaytyaWVzV29vMSs5aFNudHczQU5zM0UyT3MwOXZ4bkl1aDFaMkdpMUo3DQo0Tlg2MzM0elBDTHRCMGh4RTJVcElscXBwT1pVV3pROHFzR280VmFjZFRLUHFFWEJEVGI3R0JCN1I2SmlOOUdQDQpid3RtM2VuQnJoZDVyQ1RoWjF2UGJrQUtNbEdwNWlLTk1kMkxqV2pGeTNQUWd1ekVSaTl5cm5xb1NPN1N2TTA0DQpvWmFrZkw2ODhnTjdNb2t2TC9qQVhmSUJxK3VTUHc2VnZ5SXNQWFB1T29lRXZvZ1BIRjRhZFpCamdoZHJSMXNYDQp4V2s3eFpCaUdpK2k1Z0VQM3NPQzF1bTUxK2NQNmxRVnpGK3JyR3h6cnNPWE41UEUza0FMcGEraTJvZmdtNWlaDQpBZ01CQUFHalV6QlJNQjBHQTFVZERnUVdCQlRldDhkN2ZjYnU4ZkdLdWlSQU9HaitYMjFwa2pBZkJnTlZIU01FDQpHREFXZ0JUZXQ4ZDdmY2J1OGZHS3VpUkFPR2orWDIxcGtqQVBCZ05WSFJNQkFmOEVCVEFEQVFIL01BMEdDU3FHDQpTSWIzRFFFQkN3VUFBNElCQVFCcHdUVERJRWc4UVJpRmZBOTFRVGMvWnQ0QWVIUG5KbkU5ejFNUVVxb2RqUk4vDQpmaXpleERMMm1QWURaODNVaCtoZk1ITkJva01MRGJOa2ZUMEZXMEgrMjFIMXp5OGwrUkhkd3F2WVFicm96ZzhvDQpydTNqdkVkNjl5ODBoQngyUEhJZnRIU2gwMmtwNlNRK3QrNlU2RDY1MTFOYjlPaTU1bGRzMi91NWxkZlNXZDltDQpZR0pnRGpMa25iaXpZQ1Z4Zk1DdlJiaW1zMHVsK1ZESmY5ck50ZU9NRCtvc21WVUNUNDlSc3M3cnpTbVVxUjAvDQpkcEhnL1pPT1N0NUp4bUthNzBRS1BsRk83dUZLNU9WN0oyU1gzLzJtZ1UveWZnOEcyMmdCdXg1aEdkOU1SNERpDQpEVkl2eXIzaHNlSC9wSVZMRlhjSkRzS2FpMGVJcE5LSnFwamJJdFpKDQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tDQo=");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate ca;
        try (InputStream certificateInputStream = new ByteArrayInputStream(decode)) {
            ca = cf.generateCertificate(certificateInputStream);
        }

        // create the keystore in code using the certificate (this is sometimes done manually)
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        return context;
    }

}

 */
