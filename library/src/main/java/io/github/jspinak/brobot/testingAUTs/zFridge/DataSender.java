package io.github.jspinak.brobot.testingAUTs.zFridge;

import io.github.jspinak.brobot.testingAUTs.TestRun;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

@Component
public class DataSender {

    /**
     * Posts the results of a test run to a REST endpoint.
     * @param testRun the test data to post
     * @param postAddress holds the receiving website's API endpoint
     */
    public void send(TestRun testRun, String postAddress) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(postAddress);

        try {
            // Define the data to send as a JSON string
            String jsonData = testRun.toJson();

            // Set the JSON data as the request entity
            StringEntity entity = new StringEntity(jsonData);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            // Send the POST request
            HttpResponse response = httpClient.execute(httpPost);

            // Handle the response (e.g., check status code)
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Response Status Code: " + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
